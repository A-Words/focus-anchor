# 项目结构草案

## 当前技术方向

- Kotlin
- Jetpack Compose
- Android App（MVP 第一阶段）
- 多模块结构，优先按 `app / core / feature` 拆分

---

## 目录划分

### `app`

应用壳层，负责：

- `Application` 入口
- `MainActivity`
- 顶层导航与全局状态装配
- 后续依赖注入与前台服务接线

### `core:model`

最稳定的领域模型，当前先放：

- `FocusSession`
- `FocusSessionSummary`
- `SuspendAnchor`
- 枚举类 `FocusMode` / `SuspendItemType`

### `core:data`

数据边界层，当前先保留：

- `FocusRepository`
- `InMemoryFocusRepository`

后续这里可以继续接：

- Room
- DataStore
- 通知与前台服务数据协调
- 中断记录持久化

### `core:designsystem`

复用 UI 基础层，当前先放：

- Theme
- 颜色与排版
- 通用区块卡片 `FocusAnchorSectionCard`

### `feature:focus`

专注主流程模块，后续承接：

- 创建会话
- 倒计时
- 轻监督提醒
- 快速挂起入口

### `feature:inbox`

稍后处理箱模块，后续承接：

- 挂起列表
- 转正式待办
- 删除 / 忽略 / 标记已处理

### `feature:summary`

专注总结模块，后续承接：

- 本次结果回顾
- 中断次数
- 挂起数量
- 下一步建议

### `feature:history`

历史记录模块，后续承接：

- 历史列表
- 基础统计
- 分心时段洞察

---

## 推荐的后续演进顺序

1. 先完成 `feature:focus` 的会话创建、倒计时和本地状态管理。
2. 再把“快速挂起”接入 `feature:inbox`，形成最小闭环。
3. 接着补 `feature:summary`，完成一次专注结束后的反馈页。
4. 最后再做 `feature:history` 和 `core:data` 的持久化实现。

---

## 包结构建议

每个 feature 内建议后续按这个方向继续细分：

- `ui/`
- `component/`
- `state/`
- `model/`（仅 feature 私有展示模型）
- `domain/`（如果该 feature 逻辑明显增长）

当前仓库先保持轻量骨架，因此先只放模块入口文件，避免过早做复杂分层。
