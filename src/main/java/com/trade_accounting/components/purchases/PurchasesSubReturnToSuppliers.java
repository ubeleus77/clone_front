package com.trade_accounting.components.purchases;

import com.trade_accounting.components.AppView;
import com.trade_accounting.components.util.GridFilter;
import com.trade_accounting.components.util.GridPaginator;
import com.trade_accounting.components.util.Notifications;
import com.trade_accounting.models.dto.ReturnToSupplierDto;
import com.trade_accounting.services.interfaces.CompanyService;
import com.trade_accounting.services.interfaces.ContractService;
import com.trade_accounting.services.interfaces.ContractorService;
import com.trade_accounting.services.interfaces.ReturnToSupplierService;
import com.trade_accounting.services.interfaces.WarehouseService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Route(value = "returnsToSuppliers", layout = AppView.class)
@PageTitle("???????????????? ??????????????????????")
@SpringComponent
@UIScope
public class PurchasesSubReturnToSuppliers extends VerticalLayout implements AfterNavigationObserver {

    private final ReturnToSupplierService returnToSupplierService;
    private final WarehouseService warehouseService;
    private final CompanyService companyService;
    private final ContractorService contractorService;
    private final ContractService contractService;

    private final Notifications notifications;
    private final ReturnToSupplierModalView modalView;

    private final List<ReturnToSupplierDto> data;

    private final Grid<ReturnToSupplierDto> grid = new Grid<>(ReturnToSupplierDto.class, false);
    private GridPaginator<ReturnToSupplierDto> paginator;
    private final GridFilter<ReturnToSupplierDto> filter;

    private final TextField textField = new TextField();

    @Autowired
    public PurchasesSubReturnToSuppliers(ReturnToSupplierService returnToSupplierService, WarehouseService warehouseService,
                                         CompanyService companyService, ContractorService contractorService, ContractService contractService,
                                         @Lazy Notifications notifications, ReturnToSupplierModalView modalView) {
        this.returnToSupplierService = returnToSupplierService;
        this.warehouseService = warehouseService;
        this.companyService = companyService;
        this.contractorService = contractorService;
        this.contractService = contractService;
        this.notifications = notifications;
        this.modalView = modalView;
        this.data = loadReturnToSuppliers();
        paginator = new GridPaginator<>(grid, data, 50);
        configureGrid();
        this.filter = new GridFilter<>(grid);
        configureFilter();
        add(configureActions(), filter, grid, paginator);
    }

    private HorizontalLayout configureActions() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(buttonQuestion(), title(), buttonRefresh(), buttonAdd(),
                buttonFilter(), filterTextField(), numberField(), valueSelect(),
                valueStatus(), valuePrint(), buttonSettings());
        horizontalLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        return horizontalLayout;
    }

    private Grid<ReturnToSupplierDto> configureGrid() {
        grid.addColumn("id").setHeader("???").setId("???");
        grid.addColumn(dto -> formatDate(dto.getDate())).setKey("date").setHeader("??????????").setSortable(true).setId("????????");
        grid.addColumn(dto -> warehouseService.getById(dto.getWarehouseId()).getName()).setHeader("???? ????????????")
                .setKey("warehouseDto").setId("???? ????????????");
        grid.addColumn(dto -> companyService.getById(dto.getCompanyId()).getName()).setHeader("??????????????????????")
                .setKey("companyDto").setId("??????????????????????");
        grid.addColumn(dto -> contractorService.getById(dto.getContractorId()).getName()).setHeader("????????????????????")
                .setKey("contractorDto").setId("????????????????????");
        grid.addColumn(this::getTotalPrice).setHeader("??????????").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(this::getIsCheckedSend)).setKey("send").setHeader("????????????????????")
                .setId("????????????????????");
        grid.addColumn(new ComponentRenderer<>(this::getIsCheckedPrint)).setKey("print").setHeader("????????????????????")
                .setId("????????????????????");
        grid.addColumn("comment").setHeader("??????????????????????").setId("??????????????????????");
        grid.setHeight("66vh");
        grid.setColumnReorderingAllowed(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemDoubleClickListener(e -> {
            ReturnToSupplierDto dto = e.getItem();
            ReturnToSupplierModalView modalView = new ReturnToSupplierModalView(returnToSupplierService,
                    companyService,
                    warehouseService,
                    contractorService,
                    contractService,
                    notifications);
            modalView.setReturnToSupplierForEdit(dto);
            modalView.open();
        });
        return grid;
    }

    private void configureFilter() {
        filter.setFieldToIntegerField("id");
        filter.setFieldToDatePicker("date");
        filter.setFieldToComboBox("send", Boolean.TRUE, Boolean.FALSE);
        filter.setFieldToComboBox("print", Boolean.TRUE, Boolean.FALSE);
        filter.onSearchClick(e -> paginator
                .setData(returnToSupplierService.searchByFilter(filter.getFilterData())));
        filter.onClearClick(e -> paginator.setData(returnToSupplierService.getAll()));
    }

    private List<ReturnToSupplierDto> loadReturnToSuppliers() {
        return returnToSupplierService.getAll();
    }

    private H2 title() {
        H2 title = new H2("???????????????? ??????????????????????");
        title.setHeight("2.2em");
        return title;
    }

    private Button buttonQuestion() {
        Button buttonQuestion = new Button(new Icon(VaadinIcon.QUESTION_CIRCLE_O));
        buttonQuestion.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Dialog modal = new Dialog();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Html content = new Html("<div><p>?????????????? ???????????????????? ?????????? ?????????????? ???? ???????????? <a href=\"#\" target=\"_blank\">??????????????</a></p>" +
                "<p>???????????? ????????????????????: <a href=\"#\" target=\"_blank\">?????????????? ????????????????????</a></p></div>");
        Button close = new Button(new Icon(VaadinIcon.CLOSE));
        close.setWidth("30px");
        close.addClickListener(e -> modal.close());
        horizontalLayout.add(content, new Div(close));
        modal.add(horizontalLayout);
        modal.setWidth("500px");
        modal.setHeight("150px");
        buttonQuestion.addClickListener(e -> modal.open());
        Shortcuts.addShortcutListener(modal, modal::close, Key.ESCAPE);
        return buttonQuestion;
    }

    private Button buttonRefresh() {
        Button button = new Button(new Icon(VaadinIcon.REFRESH));
        button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> updateList());
        return button;
    }

    private Button buttonAdd() {
        Button button = new Button("??????????????", new Icon(VaadinIcon.PLUS_CIRCLE));
        button.addClickListener(e -> modalView.open());
        updateList();
        return button;
    }

    private Button buttonFilter() {
        Button button = new Button("????????????");
        button.addClickListener(e -> filter.setVisible(!filter.isVisible()));
        return button;
    }

    private TextField filterTextField() {
        textField.setPlaceholder("?????????? ?????? ??????????????????????");
        textField.addThemeVariants(TextFieldVariant.MATERIAL_ALWAYS_FLOAT_LABEL);
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addValueChangeListener(e -> updateList(textField.getValue()));
        textField.setWidth("300px");
        return textField;
    }

    private NumberField numberField() {
        NumberField numberField = new NumberField();
        numberField.setPlaceholder("0");
        numberField.setWidth("45px");
        return numberField;
    }

    private Select<String> valueSelect() {
        Select<String> select = new Select<>();
        List<String> stringList = new ArrayList<>();
        stringList.add("????????????????");
        stringList.add("???????????????? ????????????????????????????");
        stringList.add("????????????????");
        stringList.add("?????????? ????????????????????");
        stringList.add("??????????????");
        select.setItems(stringList);
        select.setValue("????????????????");
        select.setWidth("130px");
        select.addValueChangeListener(e -> {
            if (select.getValue().equals("??????????????")) {
                deleteSelectReturnToSuppliers();
                grid.deselectAll();
                select.setValue("????????????????");
                paginator.setData(loadReturnToSuppliers());
            }
        });
        return select;
    }

    private Select<String> valueStatus() {
        Select<String> status = new Select<>();
        status.setItems("????????????");
        status.setValue("????????????");
        status.setWidth("130px");
        return status;
    }

    private Select<String> valuePrint() {
        Select<String> print = new Select<>();
        print.setItems("????????????");
        print.setValue("????????????");
        print.setWidth("130px");
        return print;
    }

    private Button buttonSettings() {
        return new Button(new Icon(VaadinIcon.COG_O));
    }

    private String formatDate(String stringDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime formatDateTime = LocalDateTime.parse(stringDate);
        return formatDateTime.format(formatter);
    }

    private void updateList() {
        grid.setItems(returnToSupplierService.getAll());
    }

    public void updateList(String nameFilter) {
        if (!(textField.getValue().equals(""))) {
            grid.setItems(returnToSupplierService.searchByString(nameFilter));
        } else {
            grid.setItems(returnToSupplierService.searchByString("null"));
        }
    }

    private String getTotalPrice(ReturnToSupplierDto dto) {
        BigDecimal totalPrice = BigDecimal.valueOf(0.0);
        return String.format("%.2f", totalPrice);
    }

    private Component getIsCheckedSend(ReturnToSupplierDto dto) {
        if (dto.getIsSend()) {
            Icon icon = new Icon(VaadinIcon.CHECK);
            icon.setColor("green");
            return icon;
        } else {
            return new Span("");
        }
    }

    private Component getIsCheckedPrint(ReturnToSupplierDto dto) {
        if (dto.getIsPrint()) {
            Icon icon = new Icon(VaadinIcon.CHECK);
            icon.setColor("green");
            return icon;
        } else {
            return new Span("");
        }
    }

    private void deleteSelectReturnToSuppliers() {
        if (!grid.getSelectedItems().isEmpty()) {
            for (ReturnToSupplierDto item : grid.getSelectedItems()) {
                returnToSupplierService.deleteById(item.getId());
                notifications.infoNotification("?????????????????? ???????????????? ?????????????? ??????????????");
            }
        } else {
            notifications.errorNotification("?????????????? ???????????????? ?????????????????? ???????????? ????????????????");
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        updateList();
    }
}
