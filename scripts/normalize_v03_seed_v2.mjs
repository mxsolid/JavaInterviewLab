import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const seedDirectory = path.resolve(scriptDirectory, '../backend/src/main/resources/seed');
const bankPath = path.join(seedDirectory, 'v03-core-complete.json');
const manifestPath = path.join(seedDirectory, 'MANIFEST.json');
const targetVersion = '2026.08.21.2';

const source = JSON.parse(fs.readFileSync(bankPath, 'utf8'));
const externalKeys = new Set();
let removedDuplicateTags = 0;
let upgradedFollowUps = 0;

const questions = source.questions.map((question) => {
  if (externalKeys.has(question.externalKey)) {
    throw new Error(`重复 externalKey: ${question.externalKey}`);
  }
  externalKeys.add(question.externalKey);

  const tags = [...new Set(question.tags)];
  removedDuplicateTags += question.tags.length - tags.length;
  const followUps = question.followUps.map((followUp) => {
    if (typeof followUp === 'string') {
      upgradedFollowUps += 1;
      return { title: followUp, referenceAnswer: null };
    }
    return followUp;
  });

  return {
    externalKey: question.externalKey,
    topic: question.topic,
    title: question.title,
    starLevel: question.starLevel,
    difficulty: question.difficulty,
    frequencyLevel: question.frequencyLevel,
    questionType: question.questionType ?? 'KNOWLEDGE',
    originType: question.originType ?? 'IMPORTED',
    status: question.status ?? 'ENABLED',
    oneLiner: question.oneLiner,
    plainExplanation: question.plainExplanation,
    designReason: question.designReason,
    commonMistakes: question.commonMistakes,
    scorePoints: question.scorePoints,
    answers: question.answers,
    followUps,
    tags,
    sourceVersion: question.sourceVersion,
  };
});

const normalized = {
  seedPack: source.seedPack,
  version: targetVersion,
  mode: 'UPSERT',
  categories: source.categories,
  topics: source.topics,
  questions,
};
const bankBytes = Buffer.from(`${JSON.stringify(normalized, null, 2)}\n`, 'utf8');
fs.writeFileSync(bankPath, bankBytes);
const checksum = crypto.createHash('sha256').update(bankBytes).digest('hex');

const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
manifest.version = targetVersion;
manifest.sha256 = checksum;
manifest.note = 'V2 规范化：补齐 mode/questionType/originType/status，去除重复标签；旧字符串追问转为对象，缺失的 referenceAnswer 保持 null，不编造内容。';
fs.writeFileSync(manifestPath, `${JSON.stringify(manifest, null, 2)}\n`, 'utf8');

process.stdout.write(JSON.stringify({
  version: targetVersion,
  questionCount: questions.length,
  removedDuplicateTags,
  upgradedFollowUps,
  checksum,
}, null, 2));
