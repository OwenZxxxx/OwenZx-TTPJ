# Git 工作流与分支策略

本项目使用 GitHub 托管代码，并采用简化的 Git Flow 工作流，目标是：

- 保持 `main` 分支始终处于可运行、可演示状态；
- 通过 `develop` 分支进行日常开发集成；
- 使用 `feature/*` 分支来开发各独立功能，保证历史清晰、职责单一。

---

## 1. 分支说明

### 1.1 `main` 分支

- 用于存放**稳定版本**；
- 每次迭代完成后，从 `develop` 合并入 `main`；
- 每个重要版本会在 `main` 上打 Tag（例如 `v0.1.0`、`v1.0.0`）；

### 1.2 `develop` 分支

- 日常开发集成分支；
- 绝大部分功能开发完成后，会先合并回 `develop`，在此进行集成测试；
- 当达到一个“阶段目标”（例如核心编辑功能全部完成）后，再将 `develop` 合并到 `main`。

### 1.3 `feature/*` 分支

- 用于开发单个功能或一个相对独立的模块；
- 命名约定示例：
  - `feature/album-picker`
  - `feature/camera-capture`
  - `feature/editor-crop`
  - `feature/editor-text`
  - `feature/save-with-watermark`
- 开发流程：
  1. 从 `develop` 拉取最新代码；
  2. 创建新的 `feature/*` 分支；
  3. 在该分支上完成功能开发与自测；
  4. 发起 Pull Request（PR）合并回 `develop`；
  5. 合并完成后删除该 `feature/*` 分支。

### 1.4 `hotfix/*` 分支（视需要使用）

- 用于修复已经发布在 `main` 分支上的紧急问题；
- 从 `main` 创建 `hotfix/*` 分支，修复后合并回 `main` 和 `develop`。

---