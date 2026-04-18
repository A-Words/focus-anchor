# AGENTS 指南

## 1. 项目定位

- 这是一个 Android Kotlin + JetBrains Compose Multiplatform 多模块 MVP 项目。
- `core/*` 与 `feature/*` 均基于 Kotlin Multiplatform，当前只启用 `androidTarget()`；`app` 仍是 Android 应用壳层。后续若要扩展 iOS / Desktop 目标，在 KMP 模块补 target 即可，不需要重搭结构。
- 产品目标不是单纯计时，而是完成“专注会话 -> 快速挂起 -> 稍后处理箱 -> 专注总结 -> 历史记录”的最小闭环。
- 产品定义以 [docs/product-overview.md](/E:/src/personal/focus-anchor/docs/product-overview.md) 为准。
- 当前工程结构以 [docs/project-structure.md](/E:/src/personal/focus-anchor/docs/project-structure.md) 为准。

## 2. 当前模块事实

- `app`：应用壳层，负责 `Application`、`MainActivity`、顶层导航、前台服务、通知样式与系统级快速挂起入口装配。
- `core:model`：稳定领域模型，当前包含 `FocusSession`、`FocusSessionSummary`、`SuspendAnchor`、`FocusMode`、`SuspendItemType`。
- `core:data`：数据边界层，当前放仓库接口、可观察会话状态和占位数据实现。
- `core:designsystem`：共享 UI 基础层，当前放 Theme、颜色、排版、通用 Compose 组件。
- `feature:focus`：专注主流程，承接会话创建、倒计时、轻监督、快速挂起入口。
- `feature:inbox`：稍后处理箱，承接挂起事项查看与后续处理。
- `feature:summary`：专注总结，承接一次会话结束后的反馈页。
- `feature:history`：历史记录，承接历史列表与后续统计扩展。

## 3. 开发约束

- 新增业务逻辑，优先放进对应 `feature` 模块，不要先堆到 `app`。
- 跨功能共享的稳定业务模型，放进 `core:model`。
- 跨功能共享的数据访问边界、仓库接口或本地数据协调逻辑，放进 `core:data`。
- 跨功能复用的 Compose UI、Theme、基础组件，放进 `core:designsystem`。
- `app` 只保留壳层和装配职责，不承载具体 feature 业务实现。
- 新功能优先沿用现有模块边界；只有在现有模块明显无法承载时，才新增模块。
- 当前项目仍处于骨架期，不要过早引入复杂分层、额外框架或大规模基础设施改造。
- 专注中的系统级通知、前台服务和外部入口装配仍归 `app`，不要把这些 Android 壳层接线下沉进 feature。

## 4. UI 与实现约束

- UI 实现优先使用 Jetpack Compose。
- 业务文案默认使用中文，并与现有产品文档表述保持一致。
- 当前顶层导航基线是 `专注 / 稍后箱 / 历史 / 总结` 四个入口；除非任务明确要求，否则不要重做导航结构。
- 先补 MVP 闭环，再做增强监督、行为统计、智能化建议等扩展能力。
- 保持实现直接、可扩展，不为了“架构完整”提前抽象不存在的需求。

## 5. 变更原则

- 小步修改，避免无关重构。
- 不要顺手改产品定义；如果实现与产品文档冲突，先以文档为准，再明确标记差异。
- 不要把不相关模块一起重写，只修改当前任务真正涉及的部分。
- 优先补齐最小可用路径，再考虑视觉润色或扩展能力。
- 当改动影响模块边界、技术栈、核心闭环进度或快速开始方式时，同步更新 `AGENTS.md` 与 `README.md`，保持两者与 `docs/` 下文档一致。README 面向外部读者，AGENTS 面向协作者与 Agent，不要写成重复内容。

## 6. 最低验证要求

- 修改 Gradle 配置、模块声明或依赖关系后，至少运行 `gradle projects --no-daemon`。
- 修改功能实现类后，补最小必要验证。优先选择能直接覆盖本次改动的构建、测试或检查命令。
- 如果当前环境无法完成某项验证，明确说明没有验证到的范围和原因。

## 7. 执行时的默认判断

- 如果需求涉及专注流程，先看 `feature:focus` 是否已经有合适入口。
- 如果需求涉及挂起事项的承接或处理，优先落在 `feature:inbox`。
- 如果需求涉及会话结果反馈，优先落在 `feature:summary`。
- 如果需求涉及历史列表或统计展示，优先落在 `feature:history`。
- 如果多个 feature 共用一套展示组件或样式，再上移到 `core:designsystem`。
- 如果多个 feature 共享同一个稳定业务实体，再上移到 `core:model`。

## 8. 不要做的事

- 不要把临时 demo 代码长期留在 `app`。
- 不要在还没形成明确需求时引入 DI、复杂导航框架、远程数据层或多端适配方案。
- 不要发明仓库里不存在的流程、目录、工具或脚本。
- 不要写空泛口号；规则必须能直接指导实现落点和验证动作。
