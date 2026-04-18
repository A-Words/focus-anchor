# Focus Anchor

一个面向学习场景的 **专注中断管理助手**（Android）。

它不是普通番茄钟，而是帮助用户在专注过程中接住突发念头、延后处理干扰事项，并在必要时把用户拉回专注。

> 完整产品定义见 [docs/product-overview.md](docs/product-overview.md)。

---

## 核心闭环

**专注会话 → 快速挂起 → 稍后处理箱 → 专注总结 → 历史记录**

- **专注会话**：设定任务、时长、模式，建立一个明确的“当前唯一任务”。
- **快速挂起**：专注中突发念头，用「意图 + 可选关键词」在 1~2 秒内暂存，不中断当前任务。
- **稍后处理箱**：专注结束后统一查看、转待办或丢弃挂起事项。
- **专注总结**：会话结束时回顾时长、中断次数、挂起数量。
- **历史记录**：查看过往专注情况，为后续趋势分析留出空间。

---

## 技术栈

- Kotlin + Jetpack Compose
- Android（MVP 第一阶段）
- Gradle 多模块结构（AGP 9，Kotlin DSL）

---

## 模块结构

```
app                  应用壳层，Application / MainActivity / 顶层导航
core:model           稳定领域模型（FocusSession 等）
core:data            数据边界层（仓库接口与占位实现）
core:designsystem    共享 Theme、颜色、排版、通用 Compose 组件
feature:focus        专注主流程：会话创建、倒计时、轻监督、快速挂起入口
feature:inbox        稍后处理箱
feature:summary      专注总结页
feature:history      历史记录
```

工程结构细节见 [docs/project-structure.md](docs/project-structure.md)。

---

## 快速开始

环境要求：Android Studio（兼容 AGP 9）、JDK 17+。

```bash
# 查看模块列表
./gradlew projects

# 构建 Debug 包
./gradlew :app:assembleDebug
```

Windows 下使用 `gradlew.bat`。

---

## 当前进度

项目处于骨架期，已完成：

- 多模块骨架（app / core / feature）拆分
- `core:model` 基础领域模型
- `core:data` 可观察的内存态会话 / 挂起 / 总结状态源
- `feature:focus` 专注会话创建页 + 倒计时 + 快速挂起表单
- 前台服务通知，支持 `暂停 / 继续`、`结束`、`挂起一下`
- Android 16 `ProgressStyle` / promoted ongoing 与低版本标准通知回退

下一步演进顺序：
1. 把挂起事项补充为可处理状态，接正式待办/忽略动作
2. 补更完整的专注总结承接和继续下一轮入口
3. 做 `core:data` 持久化与 Android 17 `MetricStyle` 增强

---

## 贡献与协作约束

开发和协作规范见 [AGENTS.md](AGENTS.md)，修改代码前请先阅读其中的模块边界与变更原则。
