# UML 2.5 生图版建模图

本目录存放基于仓库现有需求、设计、数据库与源码信息，调用内置生图模型生成的一组建模图 PNG 版本，适合直接插入答辩材料或课程文档。

## 图清单

- `01-use-case-uml25-ai.png`：用例图
- `02-class-uml25-ai.png`：类图
- `03-sequence-uml25-ai.png`：顺序图
- `04-activity-uml25-ai.png`：活动图
- `05-state-machine-uml25-ai.png`：状态图
- `07-deployment-uml25-ai.png`：部署图
- `12-er-uml25-ai-reference.png`：ER 图参考版

## 使用说明

- 上述 UML 图按 UML 2.5 风格约束生成，内容依据：
  - `03-需求规格说明书/需求规格说明书-财务管理系统.md`
  - `04-软件设计说明书/软件设计说明书-财务管理系统.md`
  - `11-数据库/财务管理系统.sql`
  - `10-源代码/backend/src/main/java/com/classdesign/finance`
- 生图版更适合展示与排版，不适合作为唯一的结构真值来源。

## ER 图说明

- `12-er-uml25-ai-reference.png` 为生图参考版，可用于视觉展示。
- 若需要保证主外键关系的完全精确性，请优先使用现有矢量版：
  - `13-系统建模图/svg/12-er.svg`
  - `13-系统建模图/dot/12-er.dot`

## 建议

- 答辩 PPT 中优先使用本目录下的 PNG 版本。
- 论文、设计说明书或需要精确审阅关系时，优先引用 `svg/` 与 `dot/` 中的原始建模文件。
