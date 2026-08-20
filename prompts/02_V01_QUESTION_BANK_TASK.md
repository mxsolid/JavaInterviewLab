# Agent Task：V0.1 题库

按 A05 → A12 顺序，每次只执行一个 ID。

## A05 分类 / 专题 / 标签
完成后端 API + 前端基本管理。
不要碰学习进度。

## A06 题目 / 答案 / 追问
支持：
- starLevel
- difficulty
- frequency
- oneLiner
- plainExplanation
- designReason
- QUICK_30S / STANDARD / DEEP
- mistakes
- scorePoints
- followUps

所有枚举统一定义。

## A07 管理 CRUD
实现简洁后台编辑体验。
不要做复杂富文本，第一版 Markdown textarea 即可。

## A08 学习详情
按白色原型：
- 一句话
- 通俗讲解
- 为什么
- 多级答案
- 追问
- 易错点
- 得分点
- 相关知识

## A09 搜索
支持：
- keyword
- category
- topic
- star
- difficulty
- frequency
- status

## A10 发音
实现可复用 `EnglishTermSpeaker`。
使用浏览器 Web Speech API。
支持 `speechText` 单独指定朗读文本。

## A11 Seed
JSON 幂等导入：
- seedPack
- externalKey
- version

重复导入不得产生重复题目。

## A12 验收
按 `docs/07_测试验收与质量门禁.md` 完成。
