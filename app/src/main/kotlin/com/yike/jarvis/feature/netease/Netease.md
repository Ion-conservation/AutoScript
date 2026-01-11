# Netease 模块架构总结

## 当前架构 (Current Architecture)

你目前采用的是一种**扁平化的“驱动式”架构**：`UI -> Controller -> Executor (God Class)`。

### 优点

* **开发效率高**：逻辑集中在 `NeteaseExecutor` 中，修改逻辑时不需要在多个类/层之间跳转。
* **逻辑连贯性强**：由于使用了状态机 (`NeteaseState`) 和心跳机制，脚本的执行流程在单个文件中一目了然。
* **调试方便**：日志 (`_consoleOutput`) 直接在执行器中生成并流向 UI，响应速度快。

### 缺点

* **违反单一职责原则 (SRP)**：`NeteaseExecutor` 承担了 UI 状态管理、自动化逻辑编排、底层节点查找、心跳调度以及权限检查等过多职责。
* **难以维护与扩展**：当“网易云”脚本变得复杂（如增加 10 个新任务）或需要增加“抖音”脚本时，代码会迅速膨胀，且心跳等通用机制无法复用。
* **测试困难**：业务逻辑与 Android 特有的 `NodeTool`、`Handler` 强耦合，无法进行纯 JVM 的单元测试。

---

## Android 官方架构 (Modern Android Architecture)

Android 官方推荐的是以**数据驱动 (Data-Driven)**为核心的分层模型：

1. **UI Layer**: 处理用户交互，观察 ViewModel 暴露的状态。
2. **Domain Layer (可选)**: 包含 `UseCase`，负责纯粹的业务逻辑。
3. **Data Layer**: 由 `Repository` 和 `Data Sources` 组成，负责数据的获取与操作细节。

---

## 依据 Android 官方架构优化 Netease 模块架构

### 优化点

#### 1. 拆分“上帝类”，引入 Repository 模式

* **做法**：创建一个 `NeteaseRepository`，将 `NeteaseExecutor` 中所有以 `findXXX` 开头的方法和具体的
  `shizukuTool.tap` 调用移入其中。
* **作用**：Executor 不再关心“怎么找节点”，只关心“找到后要做什么”，实现**机制与策略分离**。

#### 2. 将脚本流程封装为 UseCase

* **做法**：将 `handleStateChange` 中的逻辑提取为 `RunNeteaseTaskUseCase`。
* **作用**：利用 Kotlin **协程 (Coroutines)** 替代 `Handler` 的 `postDelayed`，将异步回调转为顺序执行的代码结构，消除复杂的
  `isBusy` 状态位。

#### 3. 剥离通用引擎 (Automation Engine)

* **做法**：将 `heartbeatTask`（心跳）、`handleDialog`（弹窗处理）等通用逻辑抽象成一个独立的单例或辅助类。
* **作用**：这些通用能力可以服务于任何 App 的自动化脚本，而不是硬编码在网易云模块中。

#### 4. 废除 Controller，由 ViewModel 驱动

* **做法**：移除 `NeteaseController`，让 `NeteaseViewModel` 直接调用 `UseCase`。
* **作用**：ViewModel 负责观察 `A11y/Shizuku` 的 Ready 状态，并决定 UI 按钮是否可点击，简化调用链路。

---

### 优化后的架构示意图

`UI (Compose)` -> `NeteaseViewModel` -> `RunTaskUseCase` -> `NeteaseRepository` ->
`NodeTool/Shizuku`

**下一步建议：**
你可以尝试先将 `NeteaseExecutor` 里的 `findSideBar` 和 `findFreeButton` 这类方法抽离到一个新的
`NeteaseRepository` 类中，看看 `Executor` 的代码量是否变得更加清晰。如果你需要，我可以帮你写出这个
Repository 的具体结构。