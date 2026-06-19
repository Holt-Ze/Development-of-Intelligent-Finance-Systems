from __future__ import annotations

import os
import uuid
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from typing import Iterable, Sequence


DRAWIO_VERSION = "30.0.4"
OUTPUT_FILE = "finance-management-models.drawio"
SINGLE_DIR = "drawio-pages"

EDGE_BASE = (
    "edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;"
    "jettySize=auto;html=1;strokeColor=#475569;fontSize=11;"
)
TEXT_STYLE = (
    "text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=top;"
    "whiteSpace=wrap;spacingLeft=4;spacingRight=4;"
)


@dataclass
class Page:
    name: str
    width: int
    height: int
    diagram_id: str
    model: ET.Element


class PageBuilder:
    def __init__(self, name: str, page_width: int = 1600, page_height: int = 1200):
        self.name = name
        self.page_width = page_width
        self.page_height = page_height
        self._next_id = 2

        self.model = ET.Element(
            "mxGraphModel",
            {
                "dx": "1274",
                "dy": "828",
                "grid": "1",
                "gridSize": "10",
                "guides": "1",
                "tooltips": "1",
                "connect": "1",
                "arrows": "1",
                "fold": "1",
                "page": "1",
                "pageScale": "1",
                "pageWidth": str(page_width),
                "pageHeight": str(page_height),
                "math": "0",
                "shadow": "0",
            },
        )
        self.root = ET.SubElement(self.model, "root")
        ET.SubElement(self.root, "mxCell", {"id": "0"})
        ET.SubElement(self.root, "mxCell", {"id": "1", "parent": "0"})

    def next_id(self) -> str:
        value = str(self._next_id)
        self._next_id += 1
        return value

    def vertex(
        self,
        value: str,
        style: str,
        x: int,
        y: int,
        width: int,
        height: int,
        parent: str = "1",
    ) -> str:
        cell_id = self.next_id()
        cell = ET.SubElement(
            self.root,
            "mxCell",
            {
                "id": cell_id,
                "value": value,
                "style": style,
                "vertex": "1",
                "parent": parent,
            },
        )
        ET.SubElement(
            cell,
            "mxGeometry",
            {
                "x": str(x),
                "y": str(y),
                "width": str(width),
                "height": str(height),
                "as": "geometry",
            },
        )
        return cell_id

    def edge(
        self,
        source: str,
        target: str,
        value: str = "",
        style: str = EDGE_BASE,
        parent: str = "1",
        points: Sequence[tuple[int, int]] | None = None,
    ) -> str:
        cell_id = self.next_id()
        cell = ET.SubElement(
            self.root,
            "mxCell",
            {
                "id": cell_id,
                "value": value,
                "style": style,
                "edge": "1",
                "parent": parent,
                "source": source,
                "target": target,
            },
        )
        geometry = ET.SubElement(cell, "mxGeometry", {"relative": "1", "as": "geometry"})
        if points:
            point_array = ET.SubElement(geometry, "Array", {"as": "points"})
            for x, y in points:
                ET.SubElement(point_array, "mxPoint", {"x": str(x), "y": str(y)})
        return cell_id

    def html_text(
        self,
        value: str,
        x: int,
        y: int,
        width: int,
        height: int,
        parent: str = "1",
        style: str = TEXT_STYLE,
    ) -> str:
        return self.vertex(value, style, x, y, width, height, parent=parent)

    def to_page(self) -> Page:
        return Page(
            name=self.name,
            width=self.page_width,
            height=self.page_height,
            diagram_id=f"diag-{uuid.uuid4().hex[:12]}",
            model=self.model,
        )


def join_lines(lines: Iterable[str]) -> str:
    return "<br/>".join(lines)


def actor_style() -> str:
    return (
        "shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;"
        "outlineConnect=0;whiteSpace=wrap;"
    )


def use_case_style() -> str:
    return "ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#111827;fontSize=14;"


def process_style(fill: str = "#d1fae5", stroke: str = "#059669") -> str:
    return (
        f"ellipse;whiteSpace=wrap;html=1;fillColor={fill};strokeColor={stroke};"
        "fontSize=13;fontStyle=1;"
    )


def store_style() -> str:
    return "shape=cylinder3;whiteSpace=wrap;html=1;fillColor=#f8fafc;strokeColor=#475569;fontSize=12;"


def rect_style(fill: str = "#ffffff", stroke: str = "#475569", rounded: bool = True) -> str:
    rounded_flag = "1" if rounded else "0"
    return (
        f"rounded={rounded_flag};whiteSpace=wrap;html=1;fillColor={fill};"
        f"strokeColor={stroke};fontSize=13;"
    )


def lifeline_style() -> str:
    return (
        "shape=umlLifeline;perimeter=lifelinePerimeter;whiteSpace=wrap;html=1;"
        "container=1;collapsible=0;recursiveResize=0;outlineConnect=0;portConstraint=eastwest;"
        "strokeColor=#475569;fillColor=#eff6ff;fontSize=13;fontStyle=1;"
    )


def activity_edge() -> str:
    return EDGE_BASE + "endArrow=block;endFill=1;"


def dashed_edge(label: str = "") -> tuple[str, str]:
    return label, EDGE_BASE + "dashed=1;endArrow=open;endFill=0;"


def class_box(
    page: PageBuilder,
    name: str,
    attributes: Sequence[str],
    operations: Sequence[str],
    x: int,
    y: int,
    width: int,
    height: int,
    fill: str,
    stroke: str,
) -> str:
    outer = page.vertex(
        name,
        (
            "swimlane;startSize=30;fontStyle=1;align=center;html=1;"
            f"fillColor={fill};strokeColor={stroke};"
        ),
        x,
        y,
        width,
        height,
    )
    body = []
    body.extend(attributes)
    if operations:
        body.append("")
        body.append("<b>Operations</b>")
        body.extend(operations)
    page.html_text(join_lines(body), 8, 38, width - 16, height - 46, parent=outer)
    return outer


def table_box(
    page: PageBuilder,
    name: str,
    rows: Sequence[str],
    x: int,
    y: int,
    width: int,
    row_height: int = 28,
) -> str:
    table = page.vertex(
        name,
        (
            "shape=table;startSize=30;container=1;collapsible=0;childLayout=tableLayout;"
            "fixedRows=1;rowLines=1;fontStyle=1;strokeColor=#6c8ebf;fillColor=#dae8fc;html=1;"
        ),
        x,
        y,
        width,
        30 + row_height * len(rows),
    )
    row_style = (
        "shape=tableRow;horizontal=0;startSize=0;swimlaneHead=0;swimlaneBody=0;"
        "fillColor=none;collapsible=0;dropTarget=0;points=[[0,0.5],[1,0.5]];"
        "portConstraint=eastwest;fontSize=12;align=left;spacingLeft=8;html=1;"
    )
    for index, row in enumerate(rows):
        page.vertex(row, row_style, 0, 30 + index * row_height, width, row_height, parent=table)
    return table


def er_entity_box(
    page: PageBuilder,
    name: str,
    rows: Sequence[str],
    x: int,
    y: int,
    width: int,
) -> str:
    height = 54 + len(rows) * 24
    entity = page.vertex(
        name,
        (
            "swimlane;startSize=34;fontStyle=1;align=center;html=1;"
            "fillColor=#dae8fc;strokeColor=#6c8ebf;"
        ),
        x,
        y,
        width,
        height,
    )
    page.html_text(join_lines(rows), 10, 42, width - 20, height - 48, parent=entity)
    return entity


def note_box(page: PageBuilder, title: str, lines: Sequence[str], x: int, y: int, width: int) -> str:
    value = f"<b>{title}</b><br/>{join_lines(lines)}"
    height = 40 + len(lines) * 22
    return page.vertex(
        value,
        "shape=note;whiteSpace=wrap;html=1;fillColor=#fff7ed;strokeColor=#f59e0b;fontSize=12;",
        x,
        y,
        width,
        height,
    )


def build_use_case_page() -> Page:
    page = PageBuilder("用例图", 1800, 1200)
    page.vertex(
        "",
        "rounded=1;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#1d4ed8;strokeWidth=2;fontSize=20;fontStyle=1;",
        300,
        20,
        1200,
        1100,
    )
    page.html_text(
        "<div style='font-size:22px;font-weight:bold;text-align:center;'>School Department Finance Management System</div>",
        560,
        30,
        700,
        40,
        style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;",
    )

    admin = page.vertex("Administrator", actor_style(), 60, 420, 120, 170)
    finance = page.vertex("Finance Clerk", actor_style(), 1580, 450, 120, 170)

    labels = [
        ("Login / Logout", 520, 90),
        ("Manage Users", 520, 180),
        ("Manage Departments", 520, 270),
        ("Manage Accounts", 520, 360),
        ("Manage Income Categories", 520, 450),
        ("Manage Expense Categories", 520, 540),
        ("Manage Income Records", 520, 650),
        ("Manage Expense Records", 520, 760),
        ("View Reports & Dashboard", 520, 870),
        ("Backup & Restore Data", 520, 990),
    ]
    use_cases: dict[str, str] = {}
    for label, x, y in labels:
        use_cases[label] = page.vertex(label, use_case_style(), x, y, 300, 60)

    master_data = page.vertex("Enable / Disable Master Data", use_case_style(), 1100, 430, 300, 72)
    query_filter = page.vertex("Query with Filters", use_case_style(), 1080, 760, 280, 72)

    assoc_style = "edgeStyle=none;html=1;endArrow=none;startArrow=none;strokeColor=#475569;"
    admin_targets = [
        "Login / Logout",
        "Manage Users",
        "Manage Departments",
        "Manage Accounts",
        "Manage Income Categories",
        "Manage Expense Categories",
        "Manage Income Records",
        "Manage Expense Records",
        "View Reports & Dashboard",
        "Backup & Restore Data",
    ]
    for label in admin_targets:
        page.edge(admin, use_cases[label], style=assoc_style)

    finance_targets = [
        "Login / Logout",
        "Manage Income Records",
        "Manage Expense Records",
        "View Reports & Dashboard",
    ]
    for label in finance_targets:
        page.edge(finance, use_cases[label], style=assoc_style)

    page.edge(finance, query_filter, style=assoc_style)

    for label in [
        "Manage Departments",
        "Manage Accounts",
        "Manage Income Categories",
        "Manage Expense Categories",
    ]:
        page.edge(
            use_cases[label],
            master_data,
            "«include»",
            EDGE_BASE + "dashed=1;endArrow=block;endFill=0;",
        )

    for label in ["Manage Income Records", "Manage Expense Records", "View Reports & Dashboard"]:
        page.edge(
            use_cases[label],
            query_filter,
            "«include»",
            EDGE_BASE + "dashed=1;endArrow=block;endFill=0;",
        )

    return page.to_page()


def build_er_page() -> Page:
    page = PageBuilder("ER图", 2100, 1400)

    sys_user = er_entity_box(
        page,
        "sys_user",
        [
            "<b>PK</b> id",
            "username",
            "password",
            "real_name",
            "phone",
            "status",
            "created_at",
        ],
        120,
        60,
        300,
    )
    sys_user_role = er_entity_box(
        page,
        "sys_user_role",
        ["<b>PK</b> id", "<b>FK</b> user_id", "<b>FK</b> role_id"],
        760,
        120,
        280,
    )
    sys_role = er_entity_box(
        page,
        "sys_role",
        ["<b>PK</b> id", "role_code", "role_name"],
        1400,
        60,
        280,
    )

    dept_info = er_entity_box(
        page,
        "dept_info",
        [
            "<b>PK</b> id",
            "dept_code",
            "dept_name",
            "leader_name",
            "phone",
            "status",
            "created_at",
        ],
        120,
        440,
        300,
    )
    account_info = er_entity_box(
        page,
        "account_info",
        [
            "<b>PK</b> id",
            "account_code",
            "account_name",
            "owner_unit",
            "description",
            "status",
            "created_at",
        ],
        560,
        440,
        320,
    )
    income_category = er_entity_box(
        page,
        "income_category",
        ["<b>PK</b> id", "category_code", "category_name", "status", "created_at"],
        1020,
        440,
        320,
    )
    expense_category = er_entity_box(
        page,
        "expense_category",
        ["<b>PK</b> id", "category_code", "category_name", "status", "created_at"],
        1460,
        440,
        320,
    )

    income_record = er_entity_box(
        page,
        "income_record",
        [
            "<b>PK</b> id",
            "record_no",
            "<b>FK</b> dept_id",
            "<b>FK</b> account_id",
            "<b>FK</b> category_id",
            "amount",
            "occurred_on",
            "operator_name",
            "remark",
            "status",
            "created_at",
        ],
        280,
        900,
        360,
    )
    expense_record = er_entity_box(
        page,
        "expense_record",
        [
            "<b>PK</b> id",
            "record_no",
            "<b>FK</b> dept_id",
            "<b>FK</b> account_id",
            "<b>FK</b> category_id",
            "amount",
            "occurred_on",
            "operator_name",
            "remark",
            "status",
            "created_at",
        ],
        1120,
        900,
        360,
    )
    er_entity_box(
        page,
        "backup_record",
        [
            "<b>PK</b> id",
            "backup_name",
            "file_path",
            "file_size",
            "created_by",
            "created_at",
            "restored_at",
            "remark",
        ],
        1680,
        980,
        300,
    )

    note_box(page, "Legend", ["PK  Primary Key", "FK  Foreign Key", "1   One", "N   Many"], 60, 1080, 170)

    rel_style = EDGE_BASE + "endArrow=none;startArrow=none;"
    page.edge(sys_user, sys_user_role, "1 : N", rel_style)
    page.edge(sys_role, sys_user_role, "1 : N", rel_style)
    page.edge(dept_info, income_record, "1 : N", rel_style)
    page.edge(account_info, income_record, "1 : N", rel_style)
    page.edge(income_category, income_record, "1 : N", rel_style)
    page.edge(dept_info, expense_record, "1 : N", rel_style)
    page.edge(account_info, expense_record, "1 : N", rel_style)
    page.edge(expense_category, expense_record, "1 : N", rel_style)

    return page.to_page()


def build_class_page() -> Page:
    page = PageBuilder("类图", 2200, 1500)
    entity_fill = "#e8f5e9"
    entity_stroke = "#2e7d32"
    service_fill = "#e8f0fe"
    service_stroke = "#1d4ed8"

    sys_user = class_box(
        page,
        "SysUser",
        ["+id: Long", "+username: String", "+realName: String", "+phone: String", "+status: Integer"],
        [],
        40,
        40,
        250,
        230,
        entity_fill,
        entity_stroke,
    )
    sys_role = class_box(
        page,
        "SysRole",
        ["+id: Long", "+roleCode: String", "+roleName: String"],
        [],
        360,
        40,
        230,
        180,
        entity_fill,
        entity_stroke,
    )
    sys_user_role = class_box(
        page,
        "SysUserRole",
        ["+id: Long", "+userId: Long", "+roleId: Long"],
        [],
        670,
        40,
        230,
        180,
        entity_fill,
        entity_stroke,
    )
    dept_info = class_box(
        page,
        "DeptInfo",
        ["+id: Long", "+deptCode: String", "+deptName: String", "+leaderName: String", "+status: Integer"],
        [],
        970,
        40,
        250,
        230,
        entity_fill,
        entity_stroke,
    )
    account_info = class_box(
        page,
        "AccountInfo",
        ["+id: Long", "+accountCode: String", "+accountName: String", "+ownerUnit: String", "+status: Integer"],
        [],
        1290,
        40,
        250,
        230,
        entity_fill,
        entity_stroke,
    )
    income_category = class_box(
        page,
        "IncomeCategory",
        ["+id: Long", "+categoryCode: String", "+categoryName: String", "+status: Integer"],
        [],
        1610,
        40,
        250,
        210,
        entity_fill,
        entity_stroke,
    )
    expense_category = class_box(
        page,
        "ExpenseCategory",
        ["+id: Long", "+categoryCode: String", "+categoryName: String", "+status: Integer"],
        [],
        1930,
        40,
        250,
        210,
        entity_fill,
        entity_stroke,
    )

    income_record = class_box(
        page,
        "IncomeRecord",
        [
            "+id: Long",
            "+recordNo: String",
            "+deptId: Long",
            "+accountId: Long",
            "+categoryId: Long",
            "+amount: BigDecimal",
            "+occurredOn: LocalDate",
            "+status: Integer",
        ],
        [],
        960,
        470,
        320,
        300,
        entity_fill,
        entity_stroke,
    )
    expense_record = class_box(
        page,
        "ExpenseRecord",
        [
            "+id: Long",
            "+recordNo: String",
            "+deptId: Long",
            "+accountId: Long",
            "+categoryId: Long",
            "+amount: BigDecimal",
            "+occurredOn: LocalDate",
            "+status: Integer",
        ],
        [],
        1450,
        470,
        320,
        300,
        entity_fill,
        entity_stroke,
    )
    backup_record = class_box(
        page,
        "BackupRecord",
        ["+id: Long", "+backupName: String", "+filePath: String", "+fileSize: Long", "+restoredAt: LocalDateTime"],
        [],
        1860,
        490,
        280,
        240,
        entity_fill,
        entity_stroke,
    )

    auth_service = class_box(
        page,
        "AuthService",
        [],
        ["+login(request): LoginResponse", "-passwordMatches(raw, stored): boolean"],
        40,
        980,
        420,
        240,
        service_fill,
        service_stroke,
    )
    user_service = class_box(
        page,
        "UserService",
        [],
        ["+list()", "+create(request)", "+update(id, request)", "+updateStatus(id, status)"],
        520,
        980,
        320,
        250,
        service_fill,
        service_stroke,
    )
    record_service = class_box(
        page,
        "RecordService",
        [],
        [
            "+listIncomes(...)",
            "+listExpenses(...)",
            "+createIncome(request)",
            "+updateIncome(id, request)",
            "+createExpense(request)",
            "+updateExpense(id, request)",
        ],
        900,
        980,
        360,
        320,
        service_fill,
        service_stroke,
    )
    report_service = class_box(
        page,
        "ReportService",
        [],
        ["+weekly(week, deptId)", "+monthly(month, deptId)", "+yearly(year, deptId)", "+custom(startDate, endDate, deptId)", "+dashboard()"],
        1320,
        980,
        460,
        300,
        service_fill,
        service_stroke,
    )
    backup_service = class_box(
        page,
        "BackupService",
        [],
        ["+list()", "+createBackup(request)", "+download(id)", "+restore(id)"],
        1860,
        980,
        280,
        240,
        service_fill,
        service_stroke,
    )

    assoc = EDGE_BASE + "endArrow=none;startArrow=none;"
    dep = EDGE_BASE + "dashed=1;endArrow=open;endFill=0;"
    page.edge(sys_user, sys_user_role, "1 : N", assoc)
    page.edge(sys_role, sys_user_role, "1 : N", assoc)
    page.edge(dept_info, income_record, "1 : N", assoc)
    page.edge(account_info, income_record, "1 : N", assoc)
    page.edge(income_category, income_record, "1 : N", assoc)
    page.edge(dept_info, expense_record, "1 : N", assoc)
    page.edge(account_info, expense_record, "1 : N", assoc)
    page.edge(expense_category, expense_record, "1 : N", assoc)

    for target in [sys_user, sys_role]:
        page.edge(auth_service, target, style=dep)
    page.edge(user_service, sys_user, style=dep)
    for target in [dept_info, account_info, income_category, expense_category, income_record, expense_record]:
        page.edge(record_service, target, style=dep)
    for target in [income_record, expense_record]:
        page.edge(report_service, target, style=dep)
    page.edge(backup_service, backup_record, style=dep)

    return page.to_page()


def build_architecture_page() -> Page:
    page = PageBuilder("架构图", 2100, 1400)
    page.vertex(
        "",
        "rounded=1;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#0f172a;strokeWidth=2;fontSize=22;fontStyle=1;",
        300,
        40,
        1650,
        1260,
    )
    page.html_text(
        "<div style='font-size:22px;font-weight:bold;text-align:center;'>学校部门财务管理系统逻辑架构</div>",
        760,
        56,
        760,
        40,
        style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;",
    )

    admin = page.vertex("管理员", actor_style(), 60, 220, 110, 160)
    finance = page.vertex("财务员", actor_style(), 60, 470, 110, 160)

    presentation = page.vertex(
        "表示层 / Presentation",
        "swimlane;startSize=30;html=1;fillColor=#e0f2fe;strokeColor=#0284c7;fontStyle=1;",
        360,
        120,
        1520,
        220,
    )
    api = page.vertex(
        "接口层 / API",
        "swimlane;startSize=30;html=1;fillColor=#dbeafe;strokeColor=#2563eb;fontStyle=1;",
        360,
        380,
        1520,
        220,
    )
    service = page.vertex(
        "业务层 / Service",
        "swimlane;startSize=30;html=1;fillColor=#dcfce7;strokeColor=#16a34a;fontStyle=1;",
        360,
        640,
        1520,
        260,
    )
    data = page.vertex(
        "数据层 / Data",
        "swimlane;startSize=30;html=1;fillColor=#fef3c7;strokeColor=#d97706;fontStyle=1;",
        360,
        940,
        1520,
        240,
    )

    browser = page.vertex("Web 浏览器\nChrome / Edge", rect_style("#ffffff", "#0284c7"), 40, 60, 240, 100, parent=presentation)
    frontend = page.vertex("Vue 3 + Vite + Element Plus\n登录页 / 报表页 / 记录页 / 主数据页", rect_style("#ffffff", "#0284c7"), 340, 50, 380, 120, parent=presentation)
    dashboard = page.vertex("仪表盘与图表组件", rect_style("#ffffff", "#0284c7"), 810, 50, 250, 90, parent=presentation)
    master_page = page.vertex("用户 / 部门 / 账户 / 类别页面", rect_style("#ffffff", "#0284c7"), 1120, 50, 320, 90, parent=presentation)
    record_page = page.vertex("收入 / 支出录入页面", rect_style("#ffffff", "#0284c7"), 810, 150, 250, 90, parent=presentation)
    backup_page = page.vertex("备份管理页面", rect_style("#ffffff", "#0284c7"), 1120, 150, 250, 90, parent=presentation)

    auth_controller = page.vertex("AuthController", rect_style("#ffffff", "#2563eb"), 40, 70, 230, 90, parent=api)
    master_controller = page.vertex("User / Dept / Account / Category Controllers", rect_style("#ffffff", "#2563eb"), 340, 70, 420, 90, parent=api)
    record_controller = page.vertex("Income / Expense Controllers", rect_style("#ffffff", "#2563eb"), 840, 70, 330, 90, parent=api)
    report_controller = page.vertex("ReportController", rect_style("#ffffff", "#2563eb"), 1240, 70, 230, 90, parent=api)
    backup_controller = page.vertex("BackupController", rect_style("#ffffff", "#2563eb"), 40, 180, 230, 90, parent=api)

    auth_service = page.vertex("AuthService", rect_style("#ffffff", "#16a34a"), 40, 70, 220, 90, parent=service)
    user_service = page.vertex("UserService", rect_style("#ffffff", "#16a34a"), 330, 70, 220, 90, parent=service)
    master_service = page.vertex("Department / Account / Category Services", rect_style("#ffffff", "#16a34a"), 610, 70, 380, 90, parent=service)
    record_service = page.vertex("RecordService", rect_style("#ffffff", "#16a34a"), 1060, 70, 240, 90, parent=service)
    report_service = page.vertex("ReportService", rect_style("#ffffff", "#16a34a"), 1360, 70, 240, 90, parent=service)
    backup_service = page.vertex("BackupService", rect_style("#ffffff", "#16a34a"), 330, 190, 220, 90, parent=service)
    rule_box = page.vertex("核心规则:\n停用优先 / 记录有效性校验 / 统计只读有效数据", rect_style("#f0fdf4", "#16a34a"), 650, 190, 420, 110, parent=service)

    mapper = page.vertex("MyBatis-Plus Mappers", rect_style("#ffffff", "#d97706"), 60, 70, 320, 90, parent=data)
    mysql = page.vertex("MySQL 8\n10 张核心业务表", store_style(), 480, 60, 220, 120, parent=data)
    backup_dir = page.vertex("备份文件目录\nbackend/backups/", rect_style("#fff7ed", "#d97706"), 800, 70, 250, 90, parent=data)
    api_response = page.vertex("统一响应结构\ncode / message / data", rect_style("#ffffff", "#d97706"), 1140, 70, 260, 90, parent=data)

    for source in [admin, finance]:
        page.edge(source, browser, style=EDGE_BASE + "endArrow=block;")
    for source, target in [
        (browser, frontend),
        (frontend, auth_controller),
        (frontend, master_controller),
        (frontend, record_controller),
        (frontend, report_controller),
        (frontend, backup_controller),
        (record_page, record_controller),
        (dashboard, report_controller),
        (master_page, master_controller),
        (backup_page, backup_controller),
        (auth_controller, auth_service),
        (master_controller, user_service),
        (master_controller, master_service),
        (record_controller, record_service),
        (report_controller, report_service),
        (backup_controller, backup_service),
        (auth_service, mapper),
        (user_service, mapper),
        (master_service, mapper),
        (record_service, mapper),
        (report_service, mapper),
        (backup_service, mapper),
        (mapper, mysql),
        (backup_service, backup_dir),
        (report_service, api_response),
    ]:
        page.edge(source, target, style=EDGE_BASE + "endArrow=block;")

    return page.to_page()


def build_dfd_context_page() -> Page:
    page = PageBuilder("顶层DFD", 1700, 1000)

    admin = page.vertex("管理员", rect_style("#fff7ed", "#f59e0b", rounded=False), 80, 180, 180, 80)
    finance = page.vertex("财务员", rect_style("#dbeafe", "#2563eb", rounded=False), 80, 560, 180, 80)
    process = page.vertex("0. 学校部门财务管理系统", process_style("#dcfce7", "#16a34a"), 620, 300, 360, 220)
    business_db = page.vertex("D1 业务数据库", store_style(), 1260, 210, 220, 110)
    backup_store = page.vertex("D2 备份文件库", store_style(), 1260, 560, 220, 110)

    edge = EDGE_BASE + "endArrow=block;endFill=1;"
    no_arrow = EDGE_BASE + "endArrow=none;startArrow=none;"
    page.edge(admin, process, "登录请求 / 主数据维护 / 收支录入 / 报表查询 / 备份恢复", edge)
    page.edge(process, admin, "token / 维护结果 / 统计结果 / 备份结果", edge)
    page.edge(finance, process, "登录请求 / 收支录入 / 报表查询", edge)
    page.edge(process, finance, "token / 录入结果 / 统计结果", edge)
    page.edge(process, business_db, "读写用户、主数据、收支记录", no_arrow)
    page.edge(process, backup_store, "生成备份文件 / 读取恢复文件", no_arrow)

    return page.to_page()


def build_dfd_level1_page() -> Page:
    page = PageBuilder("1层DFD", 2500, 1500)

    admin = page.vertex("管理员", rect_style("#fff7ed", "#f59e0b", rounded=False), 70, 260, 190, 90)
    finance = page.vertex("财务员", rect_style("#dbeafe", "#2563eb", rounded=False), 70, 800, 190, 90)

    p1 = page.vertex("1.0\n登录与权限", process_style("#d1fae5", "#059669"), 680, 90, 220, 160)
    p2 = page.vertex("2.0\n基础信息管理", process_style("#dcfce7", "#16a34a"), 680, 360, 220, 160)
    p3 = page.vertex("3.0\n收支管理", process_style("#dcfce7", "#16a34a"), 680, 640, 220, 160)
    p4 = page.vertex("4.0\n报表统计", process_style("#cffafe", "#0891b2"), 680, 920, 220, 160)
    p5 = page.vertex("5.0\n备份与恢复", process_style("#fef3c7", "#d97706"), 680, 1200, 220, 160)

    d1 = page.vertex("D1\n用户与角色数据", store_style(), 1620, 90, 320, 120)
    d2 = page.vertex("D2\n部门 / 账户 / 类别数据", store_style(), 1620, 430, 340, 120)
    d3 = page.vertex("D3\n收入 / 支出记录数据", store_style(), 1620, 770, 340, 120)
    d4 = page.vertex("D4\n备份记录与备份文件", store_style(), 1620, 1160, 340, 120)

    arrow = EDGE_BASE + "endArrow=block;endFill=1;"
    plain = EDGE_BASE + "endArrow=none;startArrow=none;"

    for actor in [admin, finance]:
        page.edge(actor, p1, "用户名 / 密码", arrow)
        page.edge(p1, actor, "token / 角色 / 菜单", arrow)
    page.edge(p1, d1, "读取账号与角色", plain)

    page.edge(admin, p2, "主数据维护请求", arrow)
    page.edge(p2, admin, "维护结果", arrow)
    page.edge(p2, d2, "新增 / 修改 / 停用", plain)

    for actor in [admin, finance]:
        page.edge(actor, p3, "收入 / 支出录入", arrow)
        page.edge(p3, actor, "录入结果 / 查询结果", arrow)
    page.edge(p3, d2, "读取有效部门、账户、类别", plain)
    page.edge(p3, d3, "写入 / 查询记录", plain)

    for actor in [admin, finance]:
        page.edge(actor, p4, "统计条件", arrow)
        page.edge(p4, actor, "汇总结果 / 趋势数据", arrow)
    page.edge(p4, d3, "读取有效收支记录", plain)
    page.edge(p4, d2, "读取部门 / 类别名称", plain)

    page.edge(admin, p5, "创建 / 恢复备份", arrow)
    page.edge(p5, admin, "备份文件 / 恢复结果", arrow)
    page.edge(p5, d3, "读取当前业务数据", plain)
    page.edge(p5, d1, "读取账号角色数据", plain)
    page.edge(p5, d2, "读取主数据", plain)
    page.edge(p5, d4, "写入备份记录 / 备份文件", plain)

    return page.to_page()


def build_sequence_page() -> Page:
    page = PageBuilder("时序图", 2600, 1350)
    actor = page.vertex("Finance Clerk", actor_style(), 20, 10, 110, 140)

    participants = [
        ("IncomePage.vue", 250, "#eff6ff"),
        ("IncomeController", 620, "#eff6ff"),
        ("RecordService", 980, "#eff6ff"),
        ("DepartmentService /\nAccountService /\nCategoryService", 1380, "#f0fdf4"),
        ("IncomeRecordMapper", 1850, "#eff6ff"),
        ("MySQL", 2300, "#eff6ff"),
    ]
    centers: dict[str, int] = {}

    for label, center_x, fill in participants:
        centers[label] = center_x
        page.vertex(label, rect_style(fill, "#64748b", rounded=False), center_x - 120, 10, 240, 42)
        page.vertex(
            "",
            "rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#94a3b8;dashed=1;",
            center_x - 1,
            52,
            2,
            1180,
        )

    for center_x, y, height in [
        (250, 100, 820),
        (620, 180, 740),
        (980, 260, 620),
        (1380, 340, 130),
        (1850, 620, 220),
        (2300, 690, 90),
    ]:
        page.vertex("", "rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#475569;", center_x - 12, y, 24, height)

    def anchor(x: int, y: int) -> str:
        return page.vertex("", "ellipse;opacity=0;fillOpacity=0;strokeOpacity=0;", x, y, 8, 8)

    rows = {
        "1": 170,
        "2": 240,
        "3": 320,
        "4": 400,
        "5": 480,
        "6": 560,
        "7": 650,
        "8": 720,
        "9": 790,
        "10": 860,
        "11": 940,
        "12": 1010,
        "13": 1090,
    }

    actor_a1 = anchor(80, rows["1"])
    page_a1 = anchor(250, rows["1"])
    page.edge(actor_a1, page_a1, "1. fill form and click Save", "edgeStyle=none;html=1;endArrow=block;strokeColor=#111827;fontSize=11;")

    p2_from = anchor(250, rows["2"])
    p2_to = anchor(620, rows["2"])
    page.edge(p2_from, p2_to, "2. POST /incomes", "edgeStyle=none;html=1;endArrow=block;strokeColor=#111827;fontSize=11;")

    p3_from = anchor(620, rows["3"])
    p3_to = anchor(980, rows["3"])
    page.edge(p3_from, p3_to, "3. createIncome(request)", "edgeStyle=none;html=1;endArrow=block;strokeColor=#111827;fontSize=11;")

    p4_from = anchor(980, rows["4"])
    p4_to = anchor(1380, rows["4"])
    page.edge(
        p4_from,
        p4_to,
        "4. validate enabled department,\naccount, and income category",
        "edgeStyle=none;html=1;endArrow=block;strokeColor=#111827;fontSize=11;",
    )

    p5_from = anchor(1380, rows["5"])
    p5_to = anchor(980, rows["5"])
    page.edge(p5_from, p5_to, "5. validation passed", "edgeStyle=none;html=1;dashed=1;endArrow=open;endFill=0;strokeColor=#6b7280;fontSize=11;")

    loop_left = anchor(980, rows["6"])
    loop_right = anchor(1100, rows["6"])
    page.edge(loop_left, loop_right, "6. generate recordNo and createdAt", "edgeStyle=none;html=1;endArrow=block;strokeColor=#111827;fontSize=11;", points=[(1100, rows["6"] - 20), (1100, rows["6"] + 20)])

    p7_from = anchor(980, rows["7"])
    p7_to = anchor(1850, rows["7"])
    page.edge(p7_from, p7_to, "7. insert(record)", "edgeStyle=none;html=1;endArrow=block;strokeColor=#111827;fontSize=11;")

    p8_from = anchor(1850, rows["8"])
    p8_to = anchor(2300, rows["8"])
    page.edge(p8_from, p8_to, "8. INSERT income_record", "edgeStyle=none;html=1;endArrow=block;strokeColor=#111827;fontSize=11;")

    p9_from = anchor(2300, rows["9"])
    p9_to = anchor(1850, rows["9"])
    page.edge(p9_from, p9_to, "9. generated id", "edgeStyle=none;html=1;dashed=1;endArrow=open;endFill=0;strokeColor=#6b7280;fontSize=11;")

    p10_from = anchor(1850, rows["10"])
    p10_to = anchor(980, rows["10"])
    page.edge(p10_from, p10_to, "10. insert success", "edgeStyle=none;html=1;dashed=1;endArrow=open;endFill=0;strokeColor=#6b7280;fontSize=11;")

    p11_from = anchor(980, rows["11"])
    p11_to = anchor(620, rows["11"])
    page.edge(p11_from, p11_to, "11. FinanceRecordVO", "edgeStyle=none;html=1;dashed=1;endArrow=open;endFill=0;strokeColor=#6b7280;fontSize=11;")

    p12_from = anchor(620, rows["12"])
    p12_to = anchor(250, rows["12"])
    page.edge(p12_from, p12_to, "12. ApiResponse.success(data)", "edgeStyle=none;html=1;dashed=1;endArrow=open;endFill=0;strokeColor=#6b7280;fontSize=11;")

    p13_from = anchor(250, rows["13"])
    p13_to = anchor(80, rows["13"])
    page.edge(
        p13_from,
        p13_to,
        "13. refresh list and show success message",
        "edgeStyle=none;html=1;dashed=1;endArrow=open;endFill=0;strokeColor=#6b7280;fontSize=11;",
    )

    return page.to_page()


def build_activity_page() -> Page:
    page = PageBuilder("活动图", 1500, 1900)
    start = page.vertex("", "ellipse;whiteSpace=wrap;html=1;fillColor=#111827;strokeColor=#111827;", 710, 30, 40, 40)
    open_page = page.vertex("Start", rect_style("#ffffff", "#111827"), 630, 100, 200, 70)
    enter_page = page.vertex("Open Income / Expense Record Page", rect_style("#ffffff", "#111827"), 490, 220, 480, 80)
    fill_form = page.vertex("Fill department, account,\ncategory, amount, date, operator", rect_style("#ffffff", "#111827"), 470, 360, 520, 110)
    submit = page.vertex("Submit Save Request", rect_style("#ffffff", "#111827"), 560, 540, 340, 70)
    decision1 = page.vertex("Decision 1:\nRequired fields complete and\namount > 0?", "rhombus;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontSize=14;", 500, 670, 460, 180)
    error_box = page.vertex("Show error and\nreturn to form", rect_style("#ffffff", "#111827"), 90, 760, 260, 110)
    decision2 = page.vertex("Decision 2:\nDepartment / account /\ncategory enabled?", "rhombus;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontSize=14;", 500, 940, 460, 180)
    disabled_box = page.vertex("Reject save and\ntell user master\ndata is disabled", rect_style("#ffffff", "#111827"), 1120, 980, 280, 130)
    generate_no = page.vertex("Generate business\nrecord number", rect_style("#ffffff", "#111827"), 560, 1210, 340, 90)
    write_db = page.vertex("Write to income_record /\nexpense_record", rect_style("#ffffff", "#111827"), 560, 1360, 340, 90)
    refresh = page.vertex("Return result and\nrefresh list", rect_style("#ffffff", "#111827"), 560, 1510, 340, 90)
    decision3 = page.vertex("Decision 3:\nContinue to view\nreport summary?", "rhombus;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontSize=14;", 510, 1670, 440, 180)
    report = page.vertex("Call report API and\nshow trend / summary", rect_style("#ffffff", "#111827"), 1090, 1720, 290, 110)
    end = page.vertex("", "ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#111827;strokeWidth=4;", 690, 1860, 80, 80)
    page.vertex("", "ellipse;whiteSpace=wrap;html=1;fillColor=#111827;strokeColor=#111827;", 710, 1880, 40, 40)

    e = activity_edge()
    page.edge(start, open_page, style=e)
    page.edge(open_page, enter_page, style=e)
    page.edge(enter_page, fill_form, style=e)
    page.edge(fill_form, submit, style=e)
    page.edge(submit, decision1, style=e)
    page.edge(decision1, error_box, "No", e)
    page.edge(error_box, fill_form, style=e, points=[(220, 420)])
    page.edge(decision1, decision2, "Yes", e)
    page.edge(decision2, disabled_box, "No", e)
    page.edge(disabled_box, fill_form, style=e, points=[(1340, 420)])
    page.edge(decision2, generate_no, "Yes", e)
    page.edge(generate_no, write_db, style=e)
    page.edge(write_db, refresh, style=e)
    page.edge(refresh, decision3, style=e)
    page.edge(decision3, report, "Yes", e)
    page.edge(decision3, end, "No", e)
    page.edge(report, end, style=e, points=[(1230, 1900)])

    return page.to_page()


def build_deployment_page() -> Page:
    page = PageBuilder("部署图", 2200, 1100)

    node1 = page.vertex(
        'Node 1: "User Terminal"',
        "swimlane;startSize=34;html=1;fillColor=#f0fdfa;strokeColor=#0f766e;fontStyle=1;",
        30,
        180,
        420,
        300,
    )
    page.vertex("Administrator / Finance Clerk\nChrome / Edge", rect_style("#ffffff", "#0f766e"), 30, 70, 330, 110, parent=node1)

    node2 = page.vertex(
        'Node 2: "Web Frontend Node"',
        "swimlane;startSize=34;html=1;fillColor=#eff6ff;strokeColor=#1d4ed8;fontStyle=1;",
        580,
        90,
        520,
        520,
    )
    cache = page.vertex("Static Resource Cache\nHTML / JS / CSS", rect_style("#ffffff", "#1d4ed8"), 40, 90, 420, 140, parent=node2)
    spa = page.vertex("Vue 3 Frontend App\nVite build output", rect_style("#ffffff", "#1d4ed8"), 40, 300, 420, 140, parent=node2)

    node3 = page.vertex(
        'Node 3: "Application Server"',
        "swimlane;startSize=34;html=1;fillColor=#f0fdf4;strokeColor=#15803d;fontStyle=1;",
        1330,
        90,
        450,
        640,
    )
    spring = page.vertex("Spring Boot 3\nfinance-management.jar", rect_style("#ffffff", "#15803d"), 40, 100, 340, 150, parent=node3)
    backup = page.vertex("Backup Directory\nbackend/backups/", rect_style("#ffffff", "#15803d"), 80, 390, 260, 140, parent=node3)

    node4 = page.vertex(
        'Node 4: "Database Server"',
        "swimlane;startSize=34;html=1;fillColor=#fffbeb;strokeColor=#d97706;fontStyle=1;",
        1920,
        120,
        240,
        500,
    )
    mysql = page.vertex("MySQL 8\nfinance_management_system", store_style(), 30, 170, 170, 160, parent=node4)

    legend = page.vertex(
        "Legend",
        "swimlane;startSize=30;html=1;fillColor=#ffffff;strokeColor=#111827;fontStyle=1;dashed=1;",
        50,
        760,
        380,
        230,
    )
    page.html_text(
        "《device》 Node<br/>《artifact》 Artifact<br/>《storage》 Artifact (Storage)<br/>《database》 Database",
        30,
        50,
        300,
        130,
        parent=legend,
    )

    arrow = EDGE_BASE + "endArrow=block;endFill=1;"
    page.edge(node1, node2, "HTTPS", arrow)
    page.edge(node2, node3, "REST / JSON", arrow)
    page.edge(node3, node4, "JDBC", arrow)
    page.edge(spring, backup, "file read / write", EDGE_BASE + "dashed=1;endArrow=block;endFill=1;")
    page.edge(cache, spa, "load static assets", EDGE_BASE + "dashed=1;endArrow=open;endFill=0;")

    return page.to_page()


def write_drawio(pages: Sequence[Page], path: str) -> None:
    mxfile = ET.Element(
        "mxfile",
        {
            "host": "app.diagrams.net",
            "version": DRAWIO_VERSION,
            "compressed": "false",
            "type": "device",
        },
    )
    for page in pages:
        diagram = ET.SubElement(mxfile, "diagram", {"id": page.diagram_id, "name": page.name})
        diagram.append(page.model)

    tree = ET.ElementTree(mxfile)
    ET.indent(tree, space="  ")
    tree.write(path, encoding="utf-8", xml_declaration=True)


def slugify(name: str) -> str:
    mapping = {
        "用例图": "01-use-case",
        "ER图": "02-er",
        "类图": "03-class",
        "架构图": "04-architecture",
        "顶层DFD": "05-dfd-context",
        "1层DFD": "06-dfd-level1",
        "时序图": "07-sequence",
        "活动图": "08-activity",
        "部署图": "09-deployment",
    }
    return mapping[name]


def main() -> None:
    pages = [
        build_use_case_page(),
        build_er_page(),
        build_class_page(),
        build_architecture_page(),
        build_dfd_context_page(),
        build_dfd_level1_page(),
        build_sequence_page(),
        build_activity_page(),
        build_deployment_page(),
    ]
    out_path = os.path.join(os.getcwd(), OUTPUT_FILE)
    write_drawio(pages, out_path)
    single_dir = os.path.join(os.getcwd(), SINGLE_DIR)
    os.makedirs(single_dir, exist_ok=True)
    for page in pages:
        single_path = os.path.join(single_dir, f"{slugify(page.name)}.drawio")
        write_drawio([page], single_path)
    print(out_path)


if __name__ == "__main__":
    main()
