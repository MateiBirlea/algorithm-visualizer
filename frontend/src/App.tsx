import React, { useCallback, useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import './App.css';

type Role = 'ADMIN' | 'STUDENT' | 'PROFESOR';
type Algorithm =
  | 'BITONIC'
  | 'ODD_EVEN'
  | 'BATCHER_ODD_EVEN_MERGE_SORT'
  | 'PAIRWISE_SORTING_NETWORK'
  | 'BUBBLE_SORTING_NETWORK';
type Direction = 'ASC' | 'DESC';

type AuthResponse = {
  token: string;
  email: string;
  role: string;
};

type ManagedRole = 'STUDENT' | 'PROFESOR';

type AdminAccount = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: ManagedRole;
};

type SortingStep = {
  stepIndex: number;
  totalSteps?: number;
  stageIndex: number;
  leftIndex: number;
  rightIndex: number;
  leftValue: number;
  rightValue: number;
  swapped: boolean;
  arrayState: number[];
  comparedIndices?: number[];
  comparedValuesBefore?: number[];
  comparedValuesAfter?: number[];
  didSwap?: boolean;
  arrayBeforeStep?: number[];
  arrayAfterStep?: number[];
  explanation?: string;
  comparatorDirection?: Direction;
  phaseName?: string;
  comparatorDistance?: number;
  isFinalStep?: boolean;
  isArrayGloballySortedAfterStep?: boolean;
  bitonicSequenceSize?: number;
  mergeSize?: number;
  networkStage?: number;
  networkSubStage?: number;
  isBuildingBitonicSequence?: boolean;
  isBitonicMergeStep?: boolean;
  oddEvenPhase?: 'EVEN' | 'ODD';
  passNumber?: number;
};

type SortingMetrics = {
  totalSteps: number;
  totalComparisons: number;
  totalSwaps: number;
  executionTimeMs: number;
};

type ExecuteResponse = {
  steps?: SortingStep[];
  metrics?: SortingMetrics;
  requestedAlgorithm?: Algorithm;
  effectiveAlgorithm?: Algorithm;
};

type ClassroomSummary = {
  id: number;
  name: string;
  description?: string;
  joinCode: string;
  teacherId: string;
  studentCount: number;
  assignmentCount: number;
};

type AssignmentSummary = {
  id: number;
  title: string;
  description?: string;
  algorithm: Algorithm;
  direction: Direction;
  inputData: string;
  dueDate?: string;
  classId: number;
  status: 'DRAFT' | 'PUBLISHED';
};

type TeacherDashboard = {
  totalClasses: number;
  totalStudents: number;
  totalAssignments: number;
  publishedAssignments: number;
  completionRate: number;
  averageQuizScore: number;
  mostUsedAlgorithm: string;
  topStudents: string[];
  studentsNeedingAttention: string[];
};

type ClassStats = {
  classId: number;
  studentCount: number;
  assignmentCount: number;
  completionRate: number;
  averageQuizScore: number;
  averageRunCount: number;
  averageExecutionTimeMs: number;
  totalRuns: number;
  mostUsedAlgorithm: string;
  resultDistribution: string[];
};

type ClassAiAnalysis = {
  understoodConcepts: string;
  difficultConcepts: string;
  frequentMistakes: string;
  teacherRecommendations: string;
  studentsNeedingSupport: string[];
  conclusion: string;
  fullAnalysis?: string;
};

type EnrollmentInfo = {
  id: number;
  studentId: string;
  enrolledAt: string;
};

type ProgressInfo = {
  id: number | null;
  studentId: string;
  assignmentId: number;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
  runCount: number;
  totalComparisons: number;
  totalSwaps: number;
  executionTimeMs: number;
};

type AssignmentDisplayStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'LATE';

type NetworkStage = { stage: number; list: SortingStep[] };

type AlgorithmRun = {
  steps: SortingStep[];
  metrics: SortingMetrics | null;
  isExplaining: boolean;
  explainStatus: string;
};

type HistoryResult = {
  algorithm: Algorithm;
  effectiveAlgorithm: Algorithm;
  algorithmName?: Algorithm;
  sortDirection?: Direction;
  initialArray?: number[];
  finalArray?: number[];
  elementCount?: number;
  totalSteps: number;
  totalComparisons: number;
  actualComparisons?: number;
  totalSwaps: number;
  executionTimeMs: number;
  correctlySorted?: boolean;
  isInitialArraySortedAsc?: boolean;
  isInitialArraySortedDesc?: boolean;
  isFinalArraySortedCorrectly?: boolean;
  theoreticalComparisons?: number;
  comparisonMatch?: boolean;
  hasFixedComparisonCount?: boolean;
  earlyStoppingUsed?: boolean;
  comparedWithOtherAlgorithm?: boolean;
  otherAlgorithmStats?: Array<Record<string, unknown>>;
};

type HistoryEntry = {
  id: string;
  createdAt: string;
  source: 'manual' | 'file';
  mode: 'single' | 'parallel';
  direction: Direction;
  valueCount: number;
  inputValues: number[];
  results: HistoryResult[];
  isAnalyzing?: boolean;
  aiAnalysis?: string;
  aiModel?: string;
  aiLatencyMs?: number;
  aiError?: string;
  assignmentTitle?: string;
  selectedAlgorithmForAi?: Algorithm;
  algorithmAiAnalysis?: Partial<Record<Algorithm, string>>;
  algorithmAiModel?: Partial<Record<Algorithm, string>>;
  algorithmAiLatencyMs?: Partial<Record<Algorithm, number>>;
  algorithmAiError?: Partial<Record<Algorithm, string>>;
  isAnalyzingAlgorithm?: Partial<Record<Algorithm, boolean>>;
};

function apiBase(value: string | undefined, fallback: string) {
  const base = value?.trim() || fallback;
  return base.replace(/\/+$/, '');
}

const API_BASES = {
  auth: apiBase(process.env.REACT_APP_AUTH_API, '/auth-api'),
  algo: apiBase(process.env.REACT_APP_ALGO_API, '/algo-api'),
  explanation: apiBase(process.env.REACT_APP_EXPLANATION_API, '/explanation-api'),
  classroom: apiBase(process.env.REACT_APP_CLASSROOM_API, '/classroom-api'),
  user: apiBase(process.env.REACT_APP_USER_API, '/user-api'),
};

const API_URL = API_BASES.auth;
const ALGO_API = API_BASES.algo;
const EXPLANATION_API = API_BASES.explanation;
const CLASSROOM_API = API_BASES.classroom;
const MIN_VALUES = 2;
const MAX_VALUES = 10;
const MAX_FILE_VALUES = 100;
const BITONIC_POWER_OF_TWO_MESSAGE =
  'Bitonic Sort poate fi executat numai pentru un numar de elemente egal cu o putere a lui 2: 2, 4, 8, 16, 32 sau 64.';
const BATCHER_POWER_OF_TWO_MESSAGE =
  'Batcher Odd-Even Merge Sort poate fi executat doar pentru un numar de elemente egal cu o putere a lui 2 (2, 4, 8, 16, 32...).';
const AVAILABLE_ALGORITHMS: Algorithm[] = [
  'BITONIC',
  'ODD_EVEN',
  'BATCHER_ODD_EVEN_MERGE_SORT',
  'PAIRWISE_SORTING_NETWORK',
  'BUBBLE_SORTING_NETWORK',
];

const ALGORITHM_LABELS: Record<Algorithm, string> = {
  BITONIC: 'BITONIC',
  ODD_EVEN: 'ODD_EVEN',
  BATCHER_ODD_EVEN_MERGE_SORT: 'BATCHER ODD-EVEN MERGE',
  PAIRWISE_SORTING_NETWORK: 'PAIRWISE NETWORK',
  BUBBLE_SORTING_NETWORK: 'BUBBLE NETWORK',
};

function algorithmOptions() {
  return AVAILABLE_ALGORITHMS.map((algorithm) => (
    <option key={algorithm} value={algorithm}>
      {ALGORITHM_LABELS[algorithm]}
    </option>
  ));
}

function createInitialRuns(): Record<Algorithm, AlgorithmRun> {
  return AVAILABLE_ALGORITHMS.reduce((acc, algorithm) => {
    acc[algorithm] = { steps: [], metrics: null, isExplaining: false, explainStatus: '' };
    return acc;
  }, {} as Record<Algorithm, AlgorithmRun>);
}

function parseValues(input: string): number[] {
  return input
    .split(',')
    .map((v) => Number(v.trim()))
    .filter((v) => Number.isFinite(v));
}

function parseFlexibleValues(input: string): number[] {
  return input
    .split(/[\s,;]+/)
    .map((v) => Number(v.trim()))
    .filter((v) => Number.isFinite(v));
}

function isSorted(values: number[], direction: Direction) {
  return values.every((value, index) => {
    if (index === 0) {
      return true;
    }
    return direction === 'ASC' ? values[index - 1] <= value : values[index - 1] >= value;
  });
}

export function isPowerOfTwo(value: number) {
  return value > 0 && (value & (value - 1)) === 0;
}

export function validateRunInput(values: number[], maxAllowed: number, source: 'manual' | 'file', algorithm?: Algorithm) {
  if (values.length < MIN_VALUES) {
    return `Trebuie sa introduci cel putin ${MIN_VALUES} valori. Ai ${values.length}.`;
  }
  if (values.length > maxAllowed) {
    return source === 'manual'
      ? `Ai introdus ${values.length} valori. In modul normal poti rula intre ${MIN_VALUES}-${MAX_VALUES}; pentru mai multe, incarca un fisier.`
      : `Setul din fisier trebuie sa aiba cel mult ${MAX_FILE_VALUES} valori. Ai ${values.length}.`;
  }
  if (algorithm === 'BITONIC' && !isPowerOfTwo(values.length)) {
    return BITONIC_POWER_OF_TWO_MESSAGE;
  }
  if (algorithm === 'BATCHER_ODD_EVEN_MERGE_SORT' && !isPowerOfTwo(values.length)) {
    return BATCHER_POWER_OF_TWO_MESSAGE;
  }
  return '';
}

function apiErrorMessage(err: any) {
  return err?.response?.data?.message ?? err?.response?.data?.error ?? err?.message ?? String(err);
}

function theoreticalComparisonsFor(algorithm: Algorithm, elementCount: number, fallback: number) {
  if (algorithm === 'BITONIC') {
    const log2 = Math.log2(elementCount);
    return Number.isInteger(log2) ? (elementCount * log2 * (log2 + 1)) / 4 : fallback;
  }
  if (algorithm === 'ODD_EVEN' || algorithm === 'BUBBLE_SORTING_NETWORK') {
    return (elementCount * (elementCount - 1)) / 2;
  }
  return fallback;
}

function defaultPhaseFor(algorithm: Algorithm, stageIndex: number) {
  if (algorithm === 'ODD_EVEN') {
    return stageIndex % 2 === 0 ? 'EVEN_PHASE' : 'ODD_PHASE';
  }
  if (algorithm === 'BATCHER_ODD_EVEN_MERGE_SORT') {
    return 'ODD_EVEN_MERGE';
  }
  if (algorithm === 'PAIRWISE_SORTING_NETWORK') {
    return stageIndex === 0 ? 'PAIRWISE_NAIVE_COMPARE' : 'PAIRWISE_NAIVE_MERGE';
  }
  if (algorithm === 'BUBBLE_SORTING_NETWORK') {
    return 'BUBBLE_PASS';
  }
  return 'BITONIC_MERGE';
}

function withFinalAnalysisFields(result: HistoryResult, entry: HistoryEntry, allResults: HistoryResult[]) {
  const initialArray = result.initialArray ?? entry.inputValues;
  const finalArray = result.finalArray ?? [];
  const isFinalArraySortedCorrectly = finalArray.length > 0 && isSorted(finalArray, entry.direction);
  const otherAlgorithmStats = allResults
    .filter((other) => other.algorithm !== result.algorithm)
    .map((other) => ({
      algorithmName: other.algorithmName ?? other.algorithm,
      effectiveAlgorithm: other.effectiveAlgorithm,
      totalSteps: other.totalSteps,
      totalComparisons: other.totalComparisons,
      totalSwaps: other.totalSwaps,
      executionTimeMs: other.executionTimeMs,
      correctlySorted: other.correctlySorted ?? other.isFinalArraySortedCorrectly ?? false,
    }));

  return {
    ...result,
    algorithmName: result.algorithmName ?? result.algorithm,
    sortDirection: result.sortDirection ?? entry.direction,
    initialArray,
    finalArray,
    elementCount: result.elementCount ?? initialArray.length,
    correctlySorted: result.correctlySorted ?? isFinalArraySortedCorrectly,
    isInitialArraySortedAsc: result.isInitialArraySortedAsc ?? isSorted(initialArray, 'ASC'),
    isInitialArraySortedDesc: result.isInitialArraySortedDesc ?? isSorted(initialArray, 'DESC'),
    isFinalArraySortedCorrectly,
    actualComparisons: result.actualComparisons ?? result.totalComparisons,
    theoreticalComparisons: result.theoreticalComparisons ?? theoreticalComparisonsFor(result.effectiveAlgorithm, initialArray.length, result.totalComparisons),
    comparisonMatch:
      result.comparisonMatch ??
      (result.actualComparisons ?? result.totalComparisons) ===
        (result.theoreticalComparisons ?? theoreticalComparisonsFor(result.effectiveAlgorithm, initialArray.length, result.totalComparisons)),
    hasFixedComparisonCount: result.hasFixedComparisonCount ?? result.effectiveAlgorithm === 'BITONIC',
    earlyStoppingUsed: result.earlyStoppingUsed ?? false,
    comparedWithOtherAlgorithm: allResults.length > 1,
    otherAlgorithmStats,
  };
}

function toComparativeAnalysisResult(result: HistoryResult, entry: HistoryEntry) {
  const finalArray = result.finalArray ?? [];
  return {
    algorithmName: result.algorithmName ?? result.algorithm,
    executedAs: result.effectiveAlgorithm,
    totalSteps: result.totalSteps,
    totalComparisons: result.totalComparisons,
    totalSwaps: result.totalSwaps,
    executionTimeMs: result.executionTimeMs,
    correctlySorted: result.correctlySorted ?? (finalArray.length > 0 && isSorted(finalArray, entry.direction)),
    finalArray,
  };
}

function buildHistoryResult(
  algorithm: Algorithm,
  effectiveAlgorithm: Algorithm,
  direction: Direction,
  initialArray: number[],
  finalArray: number[],
  metrics: SortingMetrics
): HistoryResult {
  const correctlySorted = isSorted(finalArray, direction);
  const theoreticalComparisons = theoreticalComparisonsFor(effectiveAlgorithm, initialArray.length, metrics.totalComparisons);
  return {
    algorithm,
    effectiveAlgorithm,
    algorithmName: algorithm,
    sortDirection: direction,
    initialArray: [...initialArray],
    finalArray: [...finalArray],
    elementCount: initialArray.length,
    totalSteps: metrics.totalSteps,
    totalComparisons: metrics.totalComparisons,
    actualComparisons: metrics.totalComparisons,
    totalSwaps: metrics.totalSwaps,
    executionTimeMs: metrics.executionTimeMs,
    correctlySorted,
    isInitialArraySortedAsc: isSorted(initialArray, 'ASC'),
    isInitialArraySortedDesc: isSorted(initialArray, 'DESC'),
    isFinalArraySortedCorrectly: correctlySorted,
    theoreticalComparisons,
    comparisonMatch: theoreticalComparisons === metrics.totalComparisons,
    hasFixedComparisonCount: effectiveAlgorithm === 'BITONIC',
    earlyStoppingUsed: false,
    comparedWithOtherAlgorithm: false,
  };
}

export function buildStageBuckets(steps: SortingStep[]): NetworkStage[] {
  const map = new Map<number, SortingStep[]>();
  steps.forEach((step) => {
    const group = map.get(step.stageIndex) ?? [];
    group.push(step);
    map.set(step.stageIndex, group);
  });
  return Array.from(map.entries())
    .sort((a, b) => a[0] - b[0])
    .map(([stage, list]) => ({ stage, list }));
}

function formatDurationMs(ms: number) {
  return ms <= 0 ? '<1' : String(ms);
}

function prettyLabel(label: string) {
  return label
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

function normalizeAnalysisKey(raw: string) {
  return raw
    .toUpperCase()
    .replace(/\s+/g, '_')
    .replace(/[^A-Z0-9_]/g, '');
}

function cleanAnalysisValue(raw: string) {
  return raw
    .replace(/^\*+\s*/, '')
    .replace(/\s*\*+$/, '')
    .trim();
}

function parseAnalysisToMap(text: string): Record<string, string> {
  const map: Record<string, string> = {};
  text
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
    .forEach((line) => {
      const idx = line.indexOf(':');
      if (idx < 0) {
        return;
      }
      const key = normalizeAnalysisKey(line.slice(0, idx).trim());
      const value = cleanAnalysisValue(line.slice(idx + 1).trim());
      // Keep first occurrence only to avoid repeated noisy outputs from model
      if (!map[key] && value) {
        map[key] = value;
      }
    });
  return map;
}

function getAnalysisFields(text: string, type: 'single' | 'comparative'): Array<{ label: string; value: string }> {
  const map = parseAnalysisToMap(text);
  if (type === 'comparative') {
    const keys = [
      'ALGORITMI_ANALIZATI',
      'SORTARE_CORECTA',
      'COMPARATII',
      'SWAP_URI',
      'TIMP_MS',
      'CASTIGATOR_COMPARATII',
      'CASTIGATOR_SWAP_URI',
      'CELE_MAI_MULTE_COMPARATII',
      'DIFERENTA_COMPARATII',
      'DIFERENTA_SWAP_URI',
      'CONCLUZIE_COMPARATIVA',
    ];
    return keys
      .filter((k) => map[k])
      .map((k) => ({ label: k, value: map[k] }));
  }

  // Single analysis: enforce non-comparative fields.
  // If model accidentally returns comparative keys, remap to single semantics.
  const singleMap: Record<string, string> = {
    ALGORITM: map.ALGORITM ?? map.CASTIGATOR ?? '',
    TIMP_MS: map.TIMP_MS ?? map.TIMP_CASTIGATOR_MS ?? '',
    PASI_TOTALI: map.PASI_TOTALI ?? map.PASI_CASTIGATOR ?? '',
    COMPARATII_EXECUTATE: map.COMPARATII_EXECUTATE ?? map.COMPARATII_TOTAL ?? '',
    SWAPURI_EXECUTATE: map.SWAPURI_EXECUTATE ?? map.SWAPURI_TOTAL ?? '',
    SORTARE_CORECTA: map.SORTARE_CORECTA ?? '',
    COMPARATII_TEORETICE: map.COMPARATII_TEORETICE ?? '',
    STATUS: map.STATUS ?? '',
    CONCLUZIE_STATISTICA: map.CONCLUZIE_STATISTICA ?? map.INTERPRETARE_DATE ?? '',
  };

  const keys = [
    'ALGORITM',
    'TIMP_MS',
    'PASI_TOTALI',
    'SWAPURI_EXECUTATE',
    'SORTARE_CORECTA',
    'COMPARATII_TEORETICE',
    'COMPARATII_EXECUTATE',
    'STATUS',
    'CONCLUZIE_STATISTICA',
  ];
  return keys
    .filter((k) => singleMap[k])
    .map((k) => ({ label: k, value: singleMap[k] }));
}

function App() {
  const [token, setToken] = useState('');
  const [role, setRole] = useState<Role | ''>('');
  const [view, setView] = useState<'login' | 'register'>('login');
  const [showAuthPanel, setShowAuthPanel] = useState(true);
  const [status, setStatus] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [regFirstName, setRegFirstName] = useState('');
  const [regLastName, setRegLastName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regRole, setRegRole] = useState<Role>('STUDENT');
  const [adminAccounts, setAdminAccounts] = useState<AdminAccount[]>([]);
  const [adminFirstName, setAdminFirstName] = useState('');
  const [adminLastName, setAdminLastName] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [adminRole, setAdminRole] = useState<ManagedRole>('STUDENT');
  const [isAdminLoading, setIsAdminLoading] = useState(false);
  const [teacherDashboard, setTeacherDashboard] = useState<TeacherDashboard | null>(null);
  const [teacherClasses, setTeacherClasses] = useState<ClassroomSummary[]>([]);
  const [selectedClassId, setSelectedClassId] = useState<number | null>(null);
  const [classStudents, setClassStudents] = useState<EnrollmentInfo[]>([]);
  const [classAssignments, setClassAssignments] = useState<AssignmentSummary[]>([]);
  const [classProgress, setClassProgress] = useState<ProgressInfo[]>([]);
  const [classStats, setClassStats] = useState<ClassStats | null>(null);
  const [classAiAnalysis, setClassAiAnalysis] = useState<ClassAiAnalysis | null>(null);
  const [className, setClassName] = useState('');
  const [classDescription, setClassDescription] = useState('');
  const [assignmentTitle, setAssignmentTitle] = useState('');
  const [assignmentDescription, setAssignmentDescription] = useState('');
  const [assignmentAlgorithm, setAssignmentAlgorithm] = useState<Algorithm>('BITONIC');
  const [assignmentDirection, setAssignmentDirection] = useState<Direction>('ASC');
  const [assignmentInputData, setAssignmentInputData] = useState('8,3,1,7,4,6,2,5');
  const [assignmentFileName, setAssignmentFileName] = useState('');
  const [assignmentDueDate, setAssignmentDueDate] = useState('');
  const [assignmentStatus, setAssignmentStatus] = useState<'DRAFT' | 'PUBLISHED'>('DRAFT');
  const [studentClasses, setStudentClasses] = useState<ClassroomSummary[]>([]);
  const [studentAssignments, setStudentAssignments] = useState<AssignmentSummary[]>([]);
  const [studentProgress, setStudentProgress] = useState<ProgressInfo[]>([]);
  const [joinCode, setJoinCode] = useState('');
  const [selectedStudentClassId, setSelectedStudentClassId] = useState<number | null>(null);
  const [selectedStudentAssignment, setSelectedStudentAssignment] = useState<AssignmentSummary | null>(null);
  const [studentPage, setStudentPage] = useState<'classes' | 'execution'>('classes');
  const [pendingStudentAssignment, setPendingStudentAssignment] = useState<AssignmentSummary | null>(null);
  const [isClassroomLoading, setIsClassroomLoading] = useState(false);

  const [algoValues, setAlgoValues] = useState('8,3,1,7,4,6,2,5');
  const [uploadedValues, setUploadedValues] = useState<number[]>([]);
  const [uploadedFileName, setUploadedFileName] = useState('');
  const [selectedAlgorithm, setSelectedAlgorithm] = useState<Algorithm>('BITONIC');
  const [activeSingleAlgorithm, setActiveSingleAlgorithm] = useState<Algorithm>('BITONIC');
  const [direction, setDirection] = useState<Direction>('ASC');
  const [runs, setRuns] = useState<Record<Algorithm, AlgorithmRun>>(() => createInitialRuns());
  const [lastRunMode, setLastRunMode] = useState<'single' | 'parallel'>('single');
  const [selectedViewAlgorithm, setSelectedViewAlgorithm] = useState<Algorithm>('BITONIC');
  const [inspectStepByAlgorithm, setInspectStepByAlgorithm] = useState<Partial<Record<Algorithm, number>>>({});
  const [currentStep, setCurrentStep] = useState(0);
  const [highestReachedStep, setHighestReachedStep] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isStoppedManually, setIsStoppedManually] = useState(false);
  const [speedMs, setSpeedMs] = useState(600);
  const [isRunningAlgo, setIsRunningAlgo] = useState(false);
  const [inspectNotice, setInspectNotice] = useState('');
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const [activeHistoryEntryId, setActiveHistoryEntryId] = useState<string | null>(null);

  const hasUploadedValues = uploadedValues.length > 0;
  const sourceValues = hasUploadedValues ? uploadedValues : parseValues(algoValues);
  const visibleAlgorithms = useMemo(
    () => (lastRunMode === 'parallel' ? AVAILABLE_ALGORITHMS : [activeSingleAlgorithm]),
    [lastRunMode, activeSingleAlgorithm]
  );
  const maxSteps = useMemo(() => Math.max(0, ...visibleAlgorithms.map((a) => runs[a].steps.length)), [runs, visibleAlgorithms]);
  const isInspectMode = inspectNotice.length > 0;
  const singleHistory = useMemo(() => history.filter((h) => h.mode === 'single'), [history]);
  const parallelHistory = useMemo(() => history.filter((h) => h.mode === 'parallel'), [history]);
  const hasExecution = maxSteps > 0;
  const isExecutionFinished = hasExecution && !isPlaying && currentStep >= Math.max(maxSteps - 1, 0);
  const isExecutionStopped = hasExecution && !isPlaying;
  const canInspectExecution = isExecutionStopped && (isStoppedManually || isExecutionFinished || isInspectMode);
  const inspectLimitStep = canInspectExecution ? Math.min(highestReachedStep, Math.max(maxSteps - 1, 0)) : Math.max(maxSteps - 1, 0);

  useEffect(() => {
    if (visibleAlgorithms.length === 0) {
      return;
    }
    if (!visibleAlgorithms.includes(selectedViewAlgorithm)) {
      setSelectedViewAlgorithm(visibleAlgorithms[0]);
    }
  }, [visibleAlgorithms, selectedViewAlgorithm]);

  function addHistoryEntry(entry: Omit<HistoryEntry, 'id' | 'createdAt'>) {
    const next: HistoryEntry = {
      ...entry,
      id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
      createdAt: new Date().toISOString(),
    };
    setHistory((prev) => [next, ...prev].slice(0, 25));
    setActiveHistoryEntryId(next.id);
  }

  function stopExecution() {
    if (!hasExecution || !isPlaying) {
      return;
    }
    setIsPlaying(false);
    setIsStoppedManually(true);
    setHighestReachedStep((prev) => Math.max(prev, currentStep));
    setInspectNotice('Executia a fost oprita manual.');
  }

  function resumeExecution() {
    const resumeFromStep = Math.min(highestReachedStep, Math.max(maxSteps - 1, 0));
    if (!hasExecution || !isStoppedManually || resumeFromStep >= Math.max(maxSteps - 1, 0)) {
      return;
    }
    setCurrentStep(resumeFromStep);
    setIsPlaying(true);
    setIsStoppedManually(false);
    setInspectNotice('');
  }

  function resetExecution() {
    if (!isExecutionStopped) {
      return;
    }
    setInspectStepByAlgorithm({});
    setCurrentStep(0);
    setHighestReachedStep(0);
    setIsPlaying(false);
    setIsStoppedManually(false);
    setInspectNotice('');
  }

  function getDisplayedHistoryResult(entry: HistoryEntry, result: HistoryResult) {
    const shouldBeLive =
      entry.id === activeHistoryEntryId ||
      (history.length > 0 &&
        history[0].id === entry.id &&
        (isPlaying || isRunningAlgo));

    if (!shouldBeLive) {
      return result;
    }

    const run = runs[result.algorithm];
    const executedSteps = Math.min(currentStep + 1, run.steps.length);
    const executedSwaps = run.steps.slice(0, executedSteps).filter((s) => s.swapped).length;
    const liveTime =
      result.totalSteps <= 0
        ? 0
        : Math.round((result.executionTimeMs * Math.min(executedSteps, result.totalSteps)) / result.totalSteps);

    return {
      ...result,
      totalSteps: executedSteps,
      totalComparisons: executedSteps,
      totalSwaps: executedSwaps,
      executionTimeMs: liveTime,
    };
  }

  function isHistoryEntryRunning(entry: HistoryEntry) {
    const shouldBeLive =
      entry.id === activeHistoryEntryId ||
      (history.length > 0 &&
        history[0].id === entry.id &&
        (isPlaying || isRunningAlgo));

    if (!shouldBeLive) {
      return false;
    }
    return entry.results.some((result) => {
      const run = runs[result.algorithm];
      return run.steps.length > 0 && currentStep < run.steps.length - 1;
    });
  }

  function renderHistoryEntry(entry: HistoryEntry, allowComparative: boolean) {
    const displayedResults = entry.results.map((r) => getDisplayedHistoryResult(entry, r));
    const bestTime = Math.min(...displayedResults.map((r) => r.executionTimeMs));
    const bestSteps = Math.min(...displayedResults.map((r) => r.totalSteps));
    const running = isHistoryEntryRunning(entry);

    return (
      <div className="history-item" key={entry.id}>
        <div className="history-head">
          <span className="pill subtle">{new Date(entry.createdAt).toLocaleString()}</span>
          <span className="pill subtle">{entry.mode.toUpperCase()}</span>
          <span className="pill subtle">{entry.source.toUpperCase()}</span>
          {entry.assignmentTitle && <span className="pill subtle">TEMA: {entry.assignmentTitle}</span>}
          <span className="pill subtle">{entry.valueCount} valori</span>
          <span className="pill subtle">{entry.direction}</span>
          <span className={`pill ${running ? 'pill-running' : 'pill-final'}`}>{running ? 'RUNNING' : 'FINAL'}</span>
          {allowComparative && (
            <button className="btn btn-primary" onClick={() => analyzeHistoryEntryWithAi(entry.id)} disabled={entry.isAnalyzing}>
              {entry.isAnalyzing ? 'Se analizeaza...' : 'Analiza AI comparativa'}
            </button>
          )}
        </div>

        <div className="history-table-wrap">
          <table className="history-table">
            <thead>
              <tr>
                <th>Algoritm</th>
                <th>Executat ca</th>
                <th>Pasi</th>
                <th>Comparatii</th>
                <th>Swaps</th>
                <th>Timp (ms)</th>
                <th>AI algoritm</th>
              </tr>
            </thead>
            <tbody>
              {displayedResults.map((r) => (
                <tr key={`${entry.id}-${r.algorithm}`}>
                  <td>{r.algorithm}</td>
                  <td>{r.effectiveAlgorithm}</td>
                  <td className={r.totalSteps === bestSteps ? 'best-cell' : ''}>{r.totalSteps}</td>
                  <td>{r.totalComparisons}</td>
                  <td>{r.totalSwaps}</td>
                  <td className={r.executionTimeMs === bestTime ? 'best-cell' : ''}>{formatDurationMs(r.executionTimeMs)}</td>
                  <td>
                    <button
                      className="btn btn-primary"
                      onClick={() => analyzeHistoryEntryAlgorithmWithAi(entry.id, r.algorithm)}
                      disabled={entry.isAnalyzingAlgorithm?.[r.algorithm]}
                    >
                      {entry.isAnalyzingAlgorithm?.[r.algorithm] ? 'Se analizeaza...' : `AI ${r.algorithm}`}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {entry.selectedAlgorithmForAi && entry.algorithmAiError?.[entry.selectedAlgorithmForAi] && (
          <div className="status">{entry.algorithmAiError[entry.selectedAlgorithmForAi]}</div>
        )}
        {entry.selectedAlgorithmForAi && entry.algorithmAiAnalysis?.[entry.selectedAlgorithmForAi] && (
          <div className="status subtle">
            <div className="analysis-title">
              Analiza AI observare algoritm {entry.selectedAlgorithmForAi}
              {entry.algorithmAiModel?.[entry.selectedAlgorithmForAi]
                ? ` (${entry.algorithmAiModel[entry.selectedAlgorithmForAi]}, ${entry.algorithmAiLatencyMs?.[entry.selectedAlgorithmForAi] ?? 0} ms)`
                : ''}
            </div>
            <p className="hint-line">Tip: analiza pe un singur algoritm (fara comparatie cu altii).</p>
            <table className="analysis-table">
              <tbody>
                {(() => {
                  const rawAnalysis = entry.algorithmAiAnalysis?.[entry.selectedAlgorithmForAi] ?? '';
                  const fields = getAnalysisFields(rawAnalysis, 'single');
                  if (fields.length === 0) {
                    return (
                      <tr>
                        <td colSpan={2}>
                          <pre className="analysis-pre">{rawAnalysis}</pre>
                        </td>
                      </tr>
                    );
                  }
                  return fields.map((item, idx) => (
                    <tr key={`${entry.id}-analysis-algo-${idx}`}>
                      <th>{prettyLabel(item.label)}</th>
                      <td>{item.value}</td>
                    </tr>
                  ));
                })()}
              </tbody>
            </table>
          </div>
        )}
        {entry.aiError && <div className="status">{entry.aiError}</div>}
        {entry.aiAnalysis && (
          <div className="status subtle">
            <div className="analysis-title">
              Analiza AI comparativa intre algoritmi
              {entry.aiModel ? ` (${entry.aiModel}, ${entry.aiLatencyMs ?? 0} ms)` : ''}
            </div>
            <p className="hint-line">Tip: analiza comparativa pe mai multi algoritmi, cu castigator.</p>
            <table className="analysis-table">
              <tbody>
                {(() => {
                  const fields = getAnalysisFields(entry.aiAnalysis, 'comparative');
                  if (fields.length === 0) {
                    return (
                      <tr>
                        <td colSpan={2}>
                          <pre className="analysis-pre">{entry.aiAnalysis}</pre>
                        </td>
                      </tr>
                    );
                  }
                  return fields.map((item, idx) => (
                    <tr key={`${entry.id}-analysis-${idx}`}>
                      <th>{prettyLabel(item.label)}</th>
                      <td>{item.value}</td>
                    </tr>
                  ));
                })()}
              </tbody>
            </table>
          </div>
        )}
      </div>
    );
  }

  async function analyzeHistoryEntryWithAi(entryId: string) {
    const entry = history.find((h) => h.id === entryId);
    if (!entry) {
      return;
    }

    setHistory((prev) =>
      prev.map((h) =>
        h.id === entryId
          ? { ...h, isAnalyzing: true, aiError: undefined }
          : h
      )
    );

    try {
      const res = await axios.post(`${EXPLANATION_API}/api/explanations/run-analysis/comparative`, {
        elementCount: entry.valueCount,
        sortDirection: entry.direction,
        inputArray: entry.inputValues,
        results: entry.results.map((result) => toComparativeAnalysisResult(result, entry)),
      });

      setHistory((prev) =>
        prev.map((h) =>
          h.id === entryId
            ? {
                ...h,
                aiAnalysis: res.data?.explanation ?? 'Modelul nu a returnat un text.',
                aiModel: res.data?.model,
                aiLatencyMs: res.data?.latencyMs,
                aiError: undefined,
              }
            : h
        )
      );
    } catch (err: any) {
      setHistory((prev) =>
        prev.map((h) =>
          h.id === entryId
            ? { ...h, aiError: `Nu am putut obtine analiza AI: ${err?.message ?? err}` }
            : h
        )
      );
    } finally {
      setHistory((prev) => prev.map((h) => (h.id === entryId ? { ...h, isAnalyzing: false } : h)));
    }
  }

  async function analyzeHistoryEntryAlgorithmWithAi(entryId: string, algorithm: Algorithm) {
    const entry = history.find((h) => h.id === entryId);
    if (!entry) {
      return;
    }
    const result = entry.results.find((r) => r.algorithm === algorithm);
    if (!result) {
      return;
    }

    setHistory((prev) =>
      prev.map((h) =>
        h.id === entryId
          ? {
              ...h,
              selectedAlgorithmForAi: algorithm,
              isAnalyzingAlgorithm: { ...(h.isAnalyzingAlgorithm ?? {}), [algorithm]: true },
              algorithmAiError: { ...(h.algorithmAiError ?? {}), [algorithm]: '' },
            }
          : h
      )
    );

    try {
      const res = await axios.post(`${EXPLANATION_API}/api/explanations/run-analysis/single`, {
        source: entry.source,
        sortDirection: entry.direction,
        elementCount: entry.valueCount,
        inputArray: entry.inputValues,
        result: withFinalAnalysisFields(result, entry, entry.results),
      });

      setHistory((prev) =>
        prev.map((h) =>
          h.id === entryId
            ? {
                ...h,
                selectedAlgorithmForAi: algorithm,
                algorithmAiAnalysis: { ...(h.algorithmAiAnalysis ?? {}), [algorithm]: res.data?.explanation ?? '' },
                algorithmAiModel: { ...(h.algorithmAiModel ?? {}), [algorithm]: res.data?.model ?? '' },
                algorithmAiLatencyMs: { ...(h.algorithmAiLatencyMs ?? {}), [algorithm]: res.data?.latencyMs ?? 0 },
                algorithmAiError: { ...(h.algorithmAiError ?? {}), [algorithm]: '' },
              }
            : h
        )
      );
    } catch (err: any) {
      setHistory((prev) =>
        prev.map((h) =>
          h.id === entryId
            ? {
                ...h,
                selectedAlgorithmForAi: algorithm,
                algorithmAiError: {
                  ...(h.algorithmAiError ?? {}),
                  [algorithm]: `Nu am putut obtine analiza AI pentru ${algorithm}: ${err?.message ?? err}`,
                },
              }
            : h
        )
      );
    } finally {
      setHistory((prev) =>
        prev.map((h) =>
          h.id === entryId
            ? {
                ...h,
                isAnalyzingAlgorithm: { ...(h.isAnalyzingAlgorithm ?? {}), [algorithm]: false },
              }
            : h
        )
      );
    }
  }

  async function handleRegister() {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          firstName: regFirstName,
          lastName: regLastName,
          email: regEmail,
          password: regPassword,
          role: regRole,
        }),
      });
      const body = await res.json();
      if (!res.ok) {
        setStatus(`Eroare register: ${JSON.stringify(body)}`);
        return;
      }
      const auth = body as AuthResponse;
      setToken(auth.token);
      setRole(auth.role as Role);
      setShowAuthPanel(false);
      setStatus(`Inregistrat: ${auth.email}`);
    } catch (err) {
      setStatus(`Eroare de retea: ${String(err)}`);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleLogin() {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginEmail, password: loginPassword }),
      });
      const body = await res.json();
      if (!res.ok) {
        setStatus(`Login esuat: ${JSON.stringify(body)}`);
        return;
      }
      const auth = body as AuthResponse;
      setToken(auth.token);
      setRole(auth.role as Role);
      setShowAuthPanel(false);
      setStatus(`Autentificat: ${auth.email}`);
    } catch (err) {
      setStatus(`Eroare de retea: ${String(err)}`);
    } finally {
      setIsLoading(false);
    }
  }

  const fetchAdminAccounts = useCallback(async () => {
    if (!token) {
      return;
    }
    setIsAdminLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/admin/users`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const body = await res.json();
      if (!res.ok) {
        setStatus(`Eroare incarcare conturi: ${JSON.stringify(body)}`);
        return;
      }
      setAdminAccounts(body as AdminAccount[]);
    } catch (err) {
      setStatus(`Eroare de retea: ${String(err)}`);
    } finally {
      setIsAdminLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (token && role === 'ADMIN') {
      fetchAdminAccounts();
    }
  }, [fetchAdminAccounts, role, token]);

  const authHeaders = useCallback(
    () => ({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    }),
    [token]
  );

  const loadTeacherData = useCallback(async () => {
    if (!token) {
      return;
    }
    setIsClassroomLoading(true);
    try {
      const [dashboardRes, classesRes] = await Promise.all([
        fetch(`${CLASSROOM_API}/api/teacher/dashboard`, { headers: authHeaders() }),
        fetch(`${CLASSROOM_API}/api/classes/my`, { headers: authHeaders() }),
      ]);
      if (dashboardRes.ok) {
        setTeacherDashboard((await dashboardRes.json()) as TeacherDashboard);
      }
      if (classesRes.ok) {
        const classes = (await classesRes.json()) as ClassroomSummary[];
        setTeacherClasses(classes);
        if (!selectedClassId && classes.length > 0) {
          setSelectedClassId(classes[0].id);
        }
      }
    } catch (err) {
      setStatus(`Eroare classroom: ${String(err)}`);
    } finally {
      setIsClassroomLoading(false);
    }
  }, [authHeaders, selectedClassId, token]);

  const loadClassDetails = useCallback(
    async (classId: number) => {
      if (!token) {
        return;
      }
      setIsClassroomLoading(true);
      try {
        const [studentsRes, assignmentsRes, progressRes, statsRes] = await Promise.all([
          fetch(`${CLASSROOM_API}/api/classes/${classId}/students`, { headers: authHeaders() }),
          fetch(`${CLASSROOM_API}/api/classes/${classId}/assignments`, { headers: authHeaders() }),
          fetch(`${CLASSROOM_API}/api/classes/${classId}/progress`, { headers: authHeaders() }),
          fetch(`${CLASSROOM_API}/api/classes/${classId}/stats`, { headers: authHeaders() }),
        ]);
        if (studentsRes.ok) setClassStudents((await studentsRes.json()) as EnrollmentInfo[]);
        if (assignmentsRes.ok) setClassAssignments((await assignmentsRes.json()) as AssignmentSummary[]);
        if (progressRes.ok) setClassProgress((await progressRes.json()) as ProgressInfo[]);
        if (statsRes.ok) setClassStats((await statsRes.json()) as ClassStats);
      } catch (err) {
        setStatus(`Eroare detalii clasa: ${String(err)}`);
      } finally {
        setIsClassroomLoading(false);
      }
    },
    [authHeaders, token]
  );

  const loadStudentData = useCallback(async () => {
    if (!token) {
      return;
    }
    setIsClassroomLoading(true);
    try {
      const res = await fetch(`${CLASSROOM_API}/api/classes/my`, { headers: authHeaders() });
      if (!res.ok) {
        return;
      }
      const classes = (await res.json()) as ClassroomSummary[];
      setStudentClasses(classes);
      const progressRes = await fetch(`${CLASSROOM_API}/api/students/me/progress`, { headers: authHeaders() });
      if (progressRes.ok) {
        setStudentProgress((await progressRes.json()) as ProgressInfo[]);
      }
      const activeClassId = selectedStudentClassId ?? classes[0]?.id ?? null;
      setSelectedStudentClassId(activeClassId);
      if (activeClassId) {
        const assignmentsRes = await fetch(`${CLASSROOM_API}/api/classes/${activeClassId}/assignments`, { headers: authHeaders() });
        if (assignmentsRes.ok) {
          setStudentAssignments((await assignmentsRes.json()) as AssignmentSummary[]);
        }
      }
    } catch (err) {
      setStatus(`Eroare clase elev: ${String(err)}`);
    } finally {
      setIsClassroomLoading(false);
    }
  }, [authHeaders, selectedStudentClassId, token]);

  useEffect(() => {
    if (token && role === 'PROFESOR') {
      loadTeacherData();
    }
    if (token && role === 'STUDENT') {
      loadStudentData();
    }
  }, [loadStudentData, loadTeacherData, role, token]);

  useEffect(() => {
    if (role === 'STUDENT') {
      setStudentPage('classes');
    }
  }, [role]);

  useEffect(() => {
    if (token && role === 'PROFESOR' && selectedClassId) {
      loadClassDetails(selectedClassId);
    }
  }, [loadClassDetails, role, selectedClassId, token]);

  async function createClassroom() {
    setIsClassroomLoading(true);
    try {
      const res = await fetch(`${CLASSROOM_API}/api/classes`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ name: className, description: classDescription }),
      });
      const body = await res.json();
      if (!res.ok) {
        setStatus(`Eroare creare clasa: ${JSON.stringify(body)}`);
        return;
      }
      setClassName('');
      setClassDescription('');
      setSelectedClassId((body as ClassroomSummary).id);
      await loadTeacherData();
    } finally {
      setIsClassroomLoading(false);
    }
  }

  async function deleteClassroom(classId: number) {
    setIsClassroomLoading(true);
    try {
      await fetch(`${CLASSROOM_API}/api/classes/${classId}`, { method: 'DELETE', headers: authHeaders() });
      setSelectedClassId(null);
      await loadTeacherData();
    } finally {
      setIsClassroomLoading(false);
    }
  }

  async function createAssignment() {
    if (!selectedClassId) {
      return;
    }
    if (assignmentDueDate) {
      const selected = new Date(assignmentDueDate);
      const tomorrow = new Date();
      tomorrow.setHours(0, 0, 0, 0);
      tomorrow.setDate(tomorrow.getDate() + 1);
      if (selected < tomorrow) {
        setStatus('Deadline-ul trebuie sa fie incepand de maine, nu azi sau in trecut.');
        return;
      }
    }
    setIsClassroomLoading(true);
    try {
      const res = await fetch(`${CLASSROOM_API}/api/classes/${selectedClassId}/assignments`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({
          title: assignmentTitle,
          description: assignmentDescription,
          algorithm: assignmentAlgorithm,
          direction: assignmentDirection,
          inputData: assignmentInputData,
          dueDate: assignmentDueDate || null,
          status: assignmentStatus,
        }),
      });
      const body = await res.json();
      if (!res.ok) {
        setStatus(`Eroare creare tema: ${JSON.stringify(body)}`);
        return;
      }
      setAssignmentTitle('');
      setAssignmentDescription('');
      setAssignmentFileName('');
      await loadClassDetails(selectedClassId);
      await loadTeacherData();
    } finally {
      setIsClassroomLoading(false);
    }
  }

  async function handleAssignmentFileUpload(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    const content = await file.text();
    const values = parseFlexibleValues(content);
    if (values.length < MIN_VALUES || values.length > MAX_FILE_VALUES) {
      setStatus(`Fisierul pentru tema trebuie sa aiba intre ${MIN_VALUES} si ${MAX_FILE_VALUES} valori. Ai ${values.length}.`);
      return;
    }
    setAssignmentInputData(values.join(','));
    setAssignmentFileName(file.name);
    setStatus(`Setul de date pentru tema a fost incarcat din fisier: ${file.name}`);
  }

  async function publishAssignment(assignmentId: number) {
    await fetch(`${CLASSROOM_API}/api/assignments/${assignmentId}/publish`, { method: 'POST', headers: authHeaders() });
    if (selectedClassId) {
      await loadClassDetails(selectedClassId);
    }
  }

  async function deleteAssignment(assignmentId: number) {
    await fetch(`${CLASSROOM_API}/api/assignments/${assignmentId}`, { method: 'DELETE', headers: authHeaders() });
    if (selectedClassId) {
      await loadClassDetails(selectedClassId);
      await loadTeacherData();
    }
  }

  async function joinClassroom() {
    setIsClassroomLoading(true);
    try {
      const res = await fetch(`${CLASSROOM_API}/api/classes/join`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ joinCode }),
      });
      const body = await res.json();
      if (!res.ok) {
        setStatus(`Eroare inscriere: ${JSON.stringify(body)}`);
        return;
      }
      setJoinCode('');
      await loadStudentData();
    } finally {
      setIsClassroomLoading(false);
    }
  }

  async function runAndSubmitAssignment(assignment: AssignmentSummary) {
    const progress = getStudentProgressForAssignment(assignment.id);
    const displayStatus = getAssignmentDisplayStatus(assignment, progress);
    if (displayStatus === 'COMPLETED' || displayStatus === 'LATE') {
      setStatus(displayStatus === 'COMPLETED' ? 'Tema este deja finalizata.' : 'Deadline-ul a trecut. Tema apare ca nefacuta la timp.');
      return;
    }
    setStudentPage('execution');
    setSelectedStudentAssignment(assignment);
    setSelectedAlgorithm(assignment.algorithm);
    setDirection(assignment.direction);
    setAlgoValues(assignment.inputData);
    setUploadedValues([]);
    setUploadedFileName('');
    setPendingStudentAssignment(null);
    setStatus('Tema ruleaza prin algo-service...');
    try {
      const values = parseFlexibleValues(assignment.inputData);
      const validationMessage = validateRunInput(values, MAX_FILE_VALUES, 'file', assignment.algorithm);
      if (validationMessage) {
        setStatus(validationMessage);
        return;
      }
      const startRes = await fetch(`${CLASSROOM_API}/api/assignments/${assignment.id}/start`, { method: 'POST', headers: authHeaders() });
      if (!startRes.ok) {
        setStatus('Tema nu poate fi pornita: este deja finalizata sau deadline-ul a trecut.');
        await loadStudentData();
        return;
      }
      const res = await axios.post(`${ALGO_API}/api/sorting-networks/execute`, {
        values,
        algorithm: assignment.algorithm,
        direction: assignment.direction,
      });
      const steps = (res.data.steps ?? []) as SortingStep[];
      const metrics = (res.data.metrics ?? {}) as SortingMetrics;
      const finalArray = steps.length > 0 ? steps[steps.length - 1].arrayState : values;
      const sortedOutput = finalArray.join(',');
      const submitRes = await fetch(`${CLASSROOM_API}/api/assignments/${assignment.id}/submit`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({
          algorithm: assignment.algorithm,
          direction: assignment.direction,
          inputData: assignment.inputData,
          sortedOutput,
          totalSteps: metrics.totalSteps ?? steps.length,
          totalComparisons: metrics.totalComparisons ?? steps.length,
          totalSwaps: metrics.totalSwaps ?? 0,
          executionTimeMs: metrics.executionTimeMs ?? 0,
        }),
      });
      if (!submitRes.ok) {
        setStatus('Tema nu a putut fi trimisa: este deja finalizata sau deadline-ul a trecut.');
        await loadStudentData();
        return;
      }
      await fetch(`${CLASSROOM_API}/api/assignments/${assignment.id}/quiz-results`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({
          score: 100,
          totalQuestions: 1,
          correctAnswers: 1,
          wrongAnswers: 0,
          answers: [
            {
              question: `Ce algoritm a fost rulat pentru tema ${assignment.title}?`,
              selectedAnswer: assignment.algorithm,
              correctAnswer: assignment.algorithm,
              correct: true,
              explanation: 'Quiz initial salvat automat dupa finalizarea temei.',
            },
          ],
        }),
      });
      setRuns((prev) => ({
        ...prev,
        [assignment.algorithm]: { steps, metrics, isExplaining: false, explainStatus: '' },
      }));
      setActiveSingleAlgorithm(assignment.algorithm);
      setSelectedViewAlgorithm(assignment.algorithm);
      setLastRunMode('single');
      setCurrentStep(0);
      setHighestReachedStep(0);
      setIsPlaying(true);
      setIsStoppedManually(false);
      setInspectNotice('');
      addHistoryEntry({
        source: 'manual',
        mode: 'single',
        direction: assignment.direction,
        valueCount: values.length,
        inputValues: [...values],
        assignmentTitle: assignment.title,
        results: [
          buildHistoryResult(
            assignment.algorithm,
            res.data?.effectiveAlgorithm ?? assignment.algorithm,
            assignment.direction,
            values,
            finalArray,
            {
              totalSteps: metrics.totalSteps ?? steps.length,
              totalComparisons: metrics.totalComparisons ?? steps.length,
              totalSwaps: metrics.totalSwaps ?? 0,
              executionTimeMs: metrics.executionTimeMs ?? 0,
            }
          ),
        ],
      });
      setStatus(`Tema finalizata: ${assignment.title}`);
      await loadStudentData();
    } catch (err) {
      setStatus(`Eroare la finalizarea temei: ${apiErrorMessage(err)}`);
    }
  }

  function openAssignmentExecutionPrompt(assignment: AssignmentSummary) {
    const progress = getStudentProgressForAssignment(assignment.id);
    const displayStatus = getAssignmentDisplayStatus(assignment, progress);
    if (displayStatus === 'COMPLETED' || displayStatus === 'LATE') {
      setStatus(displayStatus === 'COMPLETED' ? 'Tema este deja finalizata.' : 'Deadline-ul a trecut. Tema apare ca nefacuta la timp.');
      return;
    }
    setPendingStudentAssignment(assignment);
  }

  async function confirmAssignmentExecution() {
    if (!pendingStudentAssignment) {
      return;
    }
    await runAndSubmitAssignment(pendingStudentAssignment);
  }

  function closeAssignmentExecutionPrompt() {
    setPendingStudentAssignment(null);
  }

  async function exportClassReport(classId: number) {
    const res = await fetch(`${CLASSROOM_API}/api/classes/${classId}/report/pdf`, { headers: authHeaders() });
    if (!res.ok) {
      setStatus('Nu am putut exporta raportul PDF.');
      return;
    }
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `class-report-${classId}.pdf`;
    link.click();
    URL.revokeObjectURL(url);
  }

  function isAssignmentOverdue(assignment: AssignmentSummary) {
    return !!assignment.dueDate && new Date(assignment.dueDate).getTime() < Date.now();
  }

  function getStudentProgressForAssignment(assignmentId: number) {
    return studentProgress.find((progress) => progress.assignmentId === assignmentId);
  }

  function getAssignmentDisplayStatus(assignment: AssignmentSummary, progress?: ProgressInfo): AssignmentDisplayStatus {
    if (progress?.status === 'COMPLETED') {
      return 'COMPLETED';
    }
    if (isAssignmentOverdue(assignment)) {
      return 'LATE';
    }
    return progress?.status ?? 'NOT_STARTED';
  }

  function statusLabel(status: AssignmentDisplayStatus | ProgressInfo['status']) {
    switch (status) {
      case 'COMPLETED':
        return 'FINALIZATA';
      case 'IN_PROGRESS':
        return 'IN LUCRU';
      case 'LATE':
        return 'NEFACUTA LA TIMP';
      default:
        return 'NEINCEPUTA';
    }
  }

  function formatDueDate(dueDate?: string) {
    return dueDate ? new Date(dueDate).toLocaleString() : 'fara deadline';
  }

  function minAssignmentDueDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    const offsetMs = tomorrow.getTimezoneOffset() * 60_000;
    return new Date(tomorrow.getTime() - offsetMs).toISOString().slice(0, 16);
  }

  async function handleAdminCreateAccount() {
    if (!token) {
      return;
    }
    setIsAdminLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/admin/users`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          firstName: adminFirstName,
          lastName: adminLastName,
          email: adminEmail,
          password: adminPassword,
          role: adminRole,
        }),
      });
      const body = await res.json();
      if (!res.ok) {
        setStatus(`Eroare creare cont: ${JSON.stringify(body)}`);
        return;
      }
      setAdminAccounts((prev) => [...prev, body as AdminAccount].sort((a, b) => a.email.localeCompare(b.email)));
      setAdminFirstName('');
      setAdminLastName('');
      setAdminEmail('');
      setAdminPassword('');
      setAdminRole('STUDENT');
      setStatus(`Cont creat: ${(body as AdminAccount).email}`);
    } catch (err) {
      setStatus(`Eroare de retea: ${String(err)}`);
    } finally {
      setIsAdminLoading(false);
    }
  }

  async function handleAdminDeleteAccount(account: AdminAccount) {
    if (!token) {
      return;
    }
    setIsAdminLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/admin/users/${account.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) {
        const body = await res.json();
        setStatus(`Eroare stergere cont: ${JSON.stringify(body)}`);
        return;
      }
      setAdminAccounts((prev) => prev.filter((a) => a.id !== account.id));
      setStatus(`Cont sters: ${account.email}`);
    } catch (err) {
      setStatus(`Eroare de retea: ${String(err)}`);
    } finally {
      setIsAdminLoading(false);
    }
  }

  async function handleFileUpload(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    const content = await file.text();
    const values = parseFlexibleValues(content);
    if (values.length < MIN_VALUES || values.length > MAX_FILE_VALUES) {
      setStatus(`Fisierul trebuie sa aiba intre ${MIN_VALUES} si ${MAX_FILE_VALUES} valori. Ai ${values.length}.`);
      return;
    }
    setUploadedValues(values);
    setUploadedFileName(file.name);
    setStatus(`Fisier incarcat: ${file.name}`);
    setRuns(createInitialRuns());
    setInspectStepByAlgorithm({});
    setCurrentStep(0);
    setHighestReachedStep(0);
    setIsPlaying(false);
  }

  async function runAllAlgorithms(source: 'manual' | 'file') {
    const values = source === 'file' ? uploadedValues : parseValues(algoValues);
    const maxAllowed = source === 'file' ? MAX_FILE_VALUES : MAX_VALUES;
    if (values.length < MIN_VALUES || values.length > maxAllowed) {
      if (values.length < MIN_VALUES) {
        setStatus(`Trebuie sa introduci cel putin ${MIN_VALUES} valori. Ai ${values.length}.`);
      } else if (source === 'manual') {
        setStatus(`Ai introdus ${values.length} valori. In modul normal poti rula intre ${MIN_VALUES}-${MAX_VALUES}; pentru mai multe, incarca un fisier.`);
      } else {
        setStatus(`Setul din fisier trebuie sa aiba cel mult ${MAX_FILE_VALUES} valori. Ai ${values.length}.`);
      }
      return;
    }
    setIsRunningAlgo(true);
    setStatus('');
    setHistory([]);
    setActiveHistoryEntryId(null);
    try {
      const responses = await Promise.allSettled(
        AVAILABLE_ALGORITHMS.map((algorithm) => {
          return axios.post(`${ALGO_API}/api/sorting-networks/execute`, {
            values,
            algorithm,
            direction,
            forceRequestedAlgorithm: true,
          });
        })
      );
      const next = createInitialRuns();
      const failed: Algorithm[] = [];
      const mismatched: string[] = [];
      const historyResults: HistoryResult[] = [];
      responses.forEach((res, idx) => {
        const algorithm = AVAILABLE_ALGORITHMS[idx];
        if (res.status === 'fulfilled') {
          const data = res.value.data as ExecuteResponse;
          const metrics: SortingMetrics = data.metrics ?? {
            totalSteps: 0,
            totalComparisons: 0,
            totalSwaps: 0,
            executionTimeMs: 0,
          };
          const effectiveAlgorithm = data.effectiveAlgorithm ?? algorithm;
          if (effectiveAlgorithm !== algorithm) {
            mismatched.push(`${algorithm} executat ca ${effectiveAlgorithm}`);
            failed.push(algorithm);
            return;
          }
          const steps = data.steps ?? [];
          const finalArray = steps.length > 0 ? steps[steps.length - 1].arrayState : values;
          next[algorithm] = {
            steps,
            metrics,
            isExplaining: false,
            explainStatus: '',
          };
          historyResults.push(buildHistoryResult(algorithm, effectiveAlgorithm, direction, values, finalArray, metrics));
          return;
        }
        failed.push(algorithm);
        if (algorithm === 'BITONIC') {
          mismatched.push(apiErrorMessage(res.reason));
        }
      });
      setRuns(next);
      setLastRunMode('parallel');
      setInspectStepByAlgorithm({});
      setCurrentStep(0);
      setHighestReachedStep(0);
      setIsPlaying(true);
      setIsStoppedManually(false);
      setInspectNotice('');
      if (historyResults.length > 0) {
        addHistoryEntry({
          source,
          mode: 'parallel',
          direction,
          valueCount: values.length,
          inputValues: [...values],
          results: historyResults,
        });
      }
      if (failed.length > 0) {
        const mismatchText = mismatched.length > 0 ? ` ${mismatched.join('; ')}.` : '';
        setStatus(`Nu au rulat: ${failed.join(', ')}.${mismatchText}`);
      }
    } catch (err: any) {
      setStatus(`Eroare executie: ${apiErrorMessage(err)}`);
    } finally {
      setIsRunningAlgo(false);
    }
  }

  async function runSingleAlgorithm(source: 'manual' | 'file') {
    const values = source === 'file' ? uploadedValues : parseValues(algoValues);
    const maxAllowed = source === 'file' ? MAX_FILE_VALUES : MAX_VALUES;
    const validationMessage = validateRunInput(values, maxAllowed, source, selectedAlgorithm);
    if (validationMessage) {
      setStatus(validationMessage);
      setRuns(createInitialRuns());
      setCurrentStep(0);
      setHighestReachedStep(0);
      setIsPlaying(false);
      setIsStoppedManually(false);
      setInspectNotice('');
      return;
    }

    setIsRunningAlgo(true);
    setStatus('');
    try {
      const res = await axios.post(`${ALGO_API}/api/sorting-networks/execute`, {
        values,
        algorithm: selectedAlgorithm,
        direction,
      });
      const data = res.data as ExecuteResponse;
      const metrics: SortingMetrics = data.metrics ?? {
        totalSteps: 0,
        totalComparisons: 0,
        totalSwaps: 0,
        executionTimeMs: 0,
      };
      const next = createInitialRuns();
      const steps = data.steps ?? [];
      const finalArray = steps.length > 0 ? steps[steps.length - 1].arrayState : values;
      next[selectedAlgorithm] = {
        steps,
        metrics,
        isExplaining: false,
        explainStatus: '',
      };
      setRuns(next);
      setActiveSingleAlgorithm(selectedAlgorithm);
      setLastRunMode('single');
      setInspectStepByAlgorithm({});
      setCurrentStep(0);
      setHighestReachedStep(0);
      setIsPlaying(true);
      setIsStoppedManually(false);
      setInspectNotice('');
      addHistoryEntry({
        source,
        mode: 'single',
        direction,
        valueCount: values.length,
        inputValues: [...values],
        results: [
          buildHistoryResult(selectedAlgorithm, data.effectiveAlgorithm ?? selectedAlgorithm, direction, values, finalArray, metrics),
        ],
      });
    } catch (err: any) {
      setStatus(`Eroare executie: ${apiErrorMessage(err)}`);
    } finally {
      setIsRunningAlgo(false);
    }
  }

  function goToStep(index: number) {
    const clamped = Math.min(Math.max(index, 0), inspectLimitStep);
    setCurrentStep(clamped);
    setIsPlaying(false);
    setInspectNotice('Executia a fost pusa pe pauza pentru inspectare.');
  }

  function goToAlgorithmStep(algorithm: Algorithm, index: number, limit: number) {
    const clamped = Math.min(Math.max(index, 0), Math.max(limit, 0));
    const runFinishedWhileOthersRun = isPlaying && runs[algorithm].steps.length > 0 && currentStep >= runs[algorithm].steps.length - 1;
    if (runFinishedWhileOthersRun) {
      setInspectStepByAlgorithm((prev) => ({ ...prev, [algorithm]: clamped }));
      return;
    }
    goToStep(clamped);
  }

  async function askExplanation(algorithm: Algorithm, step: SortingStep, arrayState: number[], stepListIndex: number) {
    const runFinishedWhileOthersRun = isPlaying && runs[algorithm].steps.length > 0 && currentStep >= runs[algorithm].steps.length - 1;
    if (runFinishedWhileOthersRun) {
      setInspectStepByAlgorithm((prev) => ({ ...prev, [algorithm]: stepListIndex }));
    } else {
      setIsPlaying(false);
      setCurrentStep(stepListIndex);
      setInspectNotice('Executia a fost oprita pentru generarea explicatiei.');
    }
    setRuns((prev) => ({ ...prev, [algorithm]: { ...prev[algorithm], isExplaining: true, explainStatus: '' } }));
    const beforeArrayState = step.arrayBeforeStep ?? (stepListIndex > 0
      ? runs[algorithm].steps[stepListIndex - 1]?.arrayState ?? sourceValues
      : sourceValues);
    const afterArrayState = step.arrayAfterStep ?? step.arrayState ?? arrayState;
    const comparedValuesBefore = step.comparedValuesBefore ?? [step.leftValue, step.rightValue];
    const comparedValuesAfter = step.comparedValuesAfter ?? [
      afterArrayState[step.leftIndex] ?? step.leftValue,
      afterArrayState[step.rightIndex] ?? step.rightValue,
    ];
    const isFinalStep = step.isFinalStep ?? stepListIndex >= runs[algorithm].steps.length - 1;
    const isArrayGloballySortedBeforeStep = isSorted(beforeArrayState, direction);
    const isArrayGloballySortedAfterStep = step.isArrayGloballySortedAfterStep ?? isSorted(afterArrayState, direction);
    const comparatorDirection = step.comparatorDirection ?? direction;
    const comparatorDistance = step.comparatorDistance ?? step.rightIndex - step.leftIndex;
    const phaseName = step.phaseName ?? defaultPhaseFor(algorithm, step.stageIndex);
    const explanationAlgorithm: Algorithm =
      phaseName === 'EVEN_PHASE' || phaseName === 'ODD_PHASE' ? 'ODD_EVEN' : algorithm;
    try {
      const res = await axios.post(`${EXPLANATION_API}/api/explanations`, {
        algorithm: explanationAlgorithm,
        direction,
        algorithmName: explanationAlgorithm,
        sortDirection: direction,
        stepType: 'COMPARATOR',
        stepIndex: step.stepIndex,
        totalSteps: runs[algorithm].steps.length,
        stageIndex: step.stageIndex,
        leftIndex: step.leftIndex,
        rightIndex: step.rightIndex,
        comparedIndices: step.comparedIndices ?? [step.leftIndex, step.rightIndex],
        leftValue: step.leftValue,
        rightValue: step.rightValue,
        comparedValuesBefore,
        comparedValuesAfter,
        swapped: step.swapped,
        didSwap: step.didSwap ?? step.swapped,
        arrayState: afterArrayState,
        beforeArrayState,
        afterArrayState,
        arrayBeforeStep: beforeArrayState,
        arrayAfterStep: afterArrayState,
        comparatorDirection,
        comparatorDistance,
        phaseName,
        stageName: phaseName,
        bitonicSequenceSize: step.bitonicSequenceSize,
        mergeSize: step.mergeSize,
        networkStage: step.networkStage ?? step.stageIndex,
        networkSubStage: step.networkSubStage ?? comparatorDistance,
        isBuildingBitonicSequence: step.isBuildingBitonicSequence ?? phaseName === 'BUILD_BITONIC_SEQUENCE',
        isBitonicMergeStep: step.isBitonicMergeStep ?? phaseName === 'BITONIC_MERGE',
        oddEvenPhase: step.oddEvenPhase ?? (phaseName === 'EVEN_PHASE' ? 'EVEN' : phaseName === 'ODD_PHASE' ? 'ODD' : undefined),
        passNumber: step.passNumber ?? step.stageIndex + 1,
        isFinalStep,
        isArrayGloballySortedBeforeStep,
        isArrayGloballySortedAfterStep,
        explanationWarning: isArrayGloballySortedAfterStep
          ? ''
          : 'Vectorul poate fi intr-o stare intermediara si nu trebuie descris ca sortat global decat daca isArrayGloballySortedAfterStep=true.',
        explanationRules: [
          'Explica doar comparatorul curent.',
          'Nu spune ca vectorul este sortat global decat daca isArrayGloballySortedAfterStep=true.',
          'Pentru Bitonic, explica faptul ca pozitiile sunt comparate deoarece fac parte din reteaua Bitonic, nu pentru ca indexul mai mic trebuie mereu sa aiba valoarea mai mica.',
          'Pentru Bitonic, mentioneaza comparatorDistance, mergeSize si phaseName.',
          'Pentru Odd-Even, mentioneaza daca este faza EVEN sau ODD.',
          'Pentru Batcher Odd-Even Merge Sort, mentioneaza ca pasul face parte din reteaua odd-even merge.',
          'Pentru Pairwise Sorting Network, mentioneaza etapa pairwise si networkStage/networkSubStage.',
          'Pentru Bubble Sorting Network, mentioneaza passNumber si comparatia locala dintre vecini.',
          'Daca didSwap=true, explica exact de ce s-a facut swap conform directiei comparatorului.',
          'Daca didSwap=false, explica exact de ce nu s-a facut swap.',
          'Nu inventa justificari despre alte portiuni ale vectorului daca nu sunt in DTO.',
          'Foloseste doar valorile din DTO.',
        ],
      });
      const explanation = res.data?.explanation ?? 'Modelul nu a returnat un text.';
      setRuns((prev) => ({
        ...prev,
        [algorithm]: {
          ...prev[algorithm],
          steps: prev[algorithm].steps.map((s, idx) => (idx === stepListIndex ? { ...s, explanation } : s)),
          explainStatus: `Explicatie generata${res.data?.model ? ` (${res.data.model})` : ''}.`,
        },
      }));
    } catch (err: any) {
      setRuns((prev) => ({
        ...prev,
        [algorithm]: { ...prev[algorithm], explainStatus: `Nu am putut obtine explicatia: ${err?.message ?? err}` },
      }));
    } finally {
      setRuns((prev) => ({ ...prev, [algorithm]: { ...prev[algorithm], isExplaining: false } }));
    }
  }

  function renderAdminDashboard() {
    return (
      <div className="card highlight admin-panel">
        <div className="card-header">
          <div>
            <p className="eyebrow">Administrare conturi</p>
            <h3>Studenti si profesori</h3>
            <p className="muted">Adminul creeaza si sterge conturi. Zona de algoritmi este ascunsa pentru acest rol.</p>
          </div>
          <div className="pill-row">
            <span className="pill">Auth API: {API_URL}</span>
            <button className="btn" onClick={fetchAdminAccounts} disabled={isAdminLoading}>
              Reincarca lista
            </button>
          </div>
        </div>
        {status && <div className="status">{status}</div>}

        <div className="admin-layout">
          <div className="card subtle">
            <div>
              <p className="eyebrow">Cont nou</p>
              <h4>Creeaza utilizator</h4>
            </div>
            <div className="grid two-col">
              <div className="field">
                <label>Prenume</label>
                <input value={adminFirstName} onChange={(e) => setAdminFirstName(e.target.value)} />
              </div>
              <div className="field">
                <label>Nume</label>
                <input value={adminLastName} onChange={(e) => setAdminLastName(e.target.value)} />
              </div>
            </div>
            <div className="field">
              <label>Email</label>
              <input value={adminEmail} onChange={(e) => setAdminEmail(e.target.value)} />
            </div>
            <div className="grid two-col">
              <div className="field">
                <label>Parola</label>
                <input type="password" value={adminPassword} onChange={(e) => setAdminPassword(e.target.value)} />
              </div>
              <div className="field">
                <label>Rol</label>
                <select value={adminRole} onChange={(e) => setAdminRole(e.target.value as ManagedRole)}>
                  <option value="STUDENT">Student</option>
                  <option value="PROFESOR">Profesor</option>
                </select>
              </div>
            </div>
            <div className="actions">
              <button className="btn btn-primary" onClick={handleAdminCreateAccount} disabled={isAdminLoading}>
                {isAdminLoading ? 'Se proceseaza...' : 'Creeaza cont'}
              </button>
            </div>
          </div>

          <div className="card subtle admin-list-card">
            <div className="card-header tight">
              <div>
                <p className="eyebrow">Conturi existente</p>
                <h4>{adminAccounts.length} conturi administrabile</h4>
              </div>
            </div>
            {adminAccounts.length === 0 ? (
              <div className="status subtle">Nu exista conturi de student sau profesor.</div>
            ) : (
              <div className="admin-table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Nume</th>
                      <th>Email</th>
                      <th>Rol</th>
                      <th>Actiune</th>
                    </tr>
                  </thead>
                  <tbody>
                    {adminAccounts.map((account) => (
                      <tr key={account.id}>
                        <td>{account.firstName} {account.lastName}</td>
                        <td>{account.email}</td>
                        <td><span className="pill subtle">{account.role}</span></td>
                        <td>
                          <button className="btn btn-danger" onClick={() => handleAdminDeleteAccount(account)} disabled={isAdminLoading}>
                            Sterge
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  function renderTeacherDashboard() {
    const selectedClass = teacherClasses.find((c) => c.id === selectedClassId);
    return (
      <div className="card highlight classroom-panel">
        <div className="card-header">
          <div>
            <p className="eyebrow">Profesor</p>
            <h3>Dashboard clase si teme</h3>
            <p className="muted">Creezi clase, publici teme si urmaresti progresul elevilor.</p>
          </div>
          <button className="btn" onClick={loadTeacherData} disabled={isClassroomLoading}>Reincarca</button>
        </div>
        {status && <div className="status">{status}</div>}

        {teacherDashboard && (
          <div className="dashboard-grid">
            <span className="pill subtle">clase: {teacherDashboard.totalClasses}</span>
            <span className="pill subtle">elevi: {teacherDashboard.totalStudents}</span>
            <span className="pill subtle">teme: {teacherDashboard.totalAssignments}</span>
            <span className="pill subtle">publicate: {teacherDashboard.publishedAssignments}</span>
            <span className="pill subtle">finalizare: {teacherDashboard.completionRate.toFixed(1)}%</span>
            <span className="pill subtle">scor mediu: {teacherDashboard.averageQuizScore.toFixed(1)}</span>
            <span className="pill subtle">algoritm: {teacherDashboard.mostUsedAlgorithm}</span>
          </div>
        )}

        <div className="classroom-layout">
          <div className="card subtle">
            <p className="eyebrow">Clasa noua</p>
            <div className="field">
              <label>Nume clasa</label>
              <input value={className} onChange={(e) => setClassName(e.target.value)} />
            </div>
            <div className="field">
              <label>Descriere</label>
              <input value={classDescription} onChange={(e) => setClassDescription(e.target.value)} />
            </div>
            <button className="btn btn-primary" onClick={createClassroom} disabled={isClassroomLoading}>Creeaza clasa</button>

            <div className="class-list">
              {teacherClasses.map((c) => (
                <button key={c.id} className={`class-item ${selectedClassId === c.id ? 'class-item-active' : ''}`} onClick={() => setSelectedClassId(c.id)}>
                  <strong>{c.name}</strong>
                  <span>cod: {c.joinCode}</span>
                  <span>{c.studentCount} elevi · {c.assignmentCount} teme</span>
                </button>
              ))}
            </div>
          </div>

          <div className="card subtle">
            {selectedClass ? (
              <>
                <div className="card-header tight">
                  <div>
                    <p className="eyebrow">Detalii clasa</p>
                    <h4>{selectedClass.name}</h4>
                    <p className="hint-line">Cod inscriere: {selectedClass.joinCode}</p>
                  </div>
                  <button className="btn btn-danger" onClick={() => deleteClassroom(selectedClass.id)}>Sterge clasa</button>
                </div>

                <div className="classroom-tabs">
                  <div className="card subtle">
                    <p className="eyebrow">Elevi</p>
                    {classStudents.length === 0 ? <p className="hint-line">Nu exista elevi inscrisi.</p> : classStudents.map((s) => (
                      <div className="row-line" key={s.id}><span>{s.studentId}</span><span>{new Date(s.enrolledAt).toLocaleString()}</span></div>
                    ))}
                  </div>

                  <div className="card subtle">
                    <p className="eyebrow">Teme</p>
                    <div className="grid two-col">
                      <div className="field"><label>Titlu</label><input value={assignmentTitle} onChange={(e) => setAssignmentTitle(e.target.value)} /></div>
                      <div className="field"><label>Deadline</label><input type="datetime-local" min={minAssignmentDueDate()} value={assignmentDueDate} onChange={(e) => setAssignmentDueDate(e.target.value)} /></div>
                    </div>
                    <div className="field"><label>Descriere</label><input value={assignmentDescription} onChange={(e) => setAssignmentDescription(e.target.value)} /></div>
                    <div className="grid two-col">
                      <div className="field"><label>Algoritm</label><select value={assignmentAlgorithm} onChange={(e) => setAssignmentAlgorithm(e.target.value as Algorithm)}>{algorithmOptions()}</select></div>
                      <div className="field"><label>Directie</label><select value={assignmentDirection} onChange={(e) => setAssignmentDirection(e.target.value as Direction)}><option value="ASC">ASC</option><option value="DESC">DESC</option></select></div>
                    </div>
                    <div className="grid two-col">
                      <div className="field">
                        <label>Set date manual</label>
                        <input value={assignmentInputData} onChange={(e) => {
                          setAssignmentInputData(e.target.value);
                          setAssignmentFileName('');
                        }} />
                      </div>
                      <div className="field">
                        <label>Incarcare fisier date</label>
                        <input type="file" accept=".txt,.csv,.dat" onChange={handleAssignmentFileUpload} />
                        <p className="hint-line">Fisier: {MIN_VALUES}-{MAX_FILE_VALUES} valori.</p>
                        {assignmentFileName && <p className="hint-line file-meta">{assignmentFileName}</p>}
                      </div>
                    </div>
                    <div className="field"><label>Status</label><select value={assignmentStatus} onChange={(e) => setAssignmentStatus(e.target.value as 'DRAFT' | 'PUBLISHED')}><option value="DRAFT">DRAFT</option><option value="PUBLISHED">PUBLISHED</option></select></div>
                    <button className="btn btn-primary" onClick={createAssignment}>Creeaza tema</button>
                    <div className="assignment-list">
                      {classAssignments.map((a) => (
                        <div className="row-line" key={a.id}>
                          <span>{a.title} - {a.algorithm}/{a.direction} - {a.status} - deadline: {formatDueDate(a.dueDate)}</span>
                          <span className="actions wrap">
                            {a.status === 'DRAFT' && <button className="btn" onClick={() => publishAssignment(a.id)}>Publica</button>}
                            <button className="btn btn-danger" onClick={() => deleteAssignment(a.id)}>Sterge</button>
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="card subtle">
                    <p className="eyebrow">Progres</p>
                    {classProgress.length === 0 ? <p className="hint-line">Nu exista progres inregistrat.</p> : classProgress.map((p) => (
                      <div className="row-line" key={`${p.assignmentId}-${p.studentId}`}>
                        {(() => {
                          const assignment = classAssignments.find((a) => a.id === p.assignmentId);
                          const displayStatus = assignment ? getAssignmentDisplayStatus(assignment, p) : p.status;
                          return (
                            <>
                              <span>{p.studentId} - tema #{p.assignmentId} - {statusLabel(displayStatus)}</span>
                              <span>rulari {p.runCount} - comp {p.totalComparisons} - swaps {p.totalSwaps} - {p.executionTimeMs} ms</span>
                            </>
                          );
                        })()}
                      </div>
                    ))}
                  </div>

                  <div className="card subtle">
                    <p className="eyebrow">Statistici si AI</p>
                    {classStats && (
                      <div className="dashboard-grid">
                        <span className="pill subtle">finalizare {classStats.completionRate.toFixed(1)}%</span>
                        <span className="pill subtle">scor {classStats.averageQuizScore.toFixed(1)}</span>
                        <span className="pill subtle">rulari {classStats.totalRuns}</span>
                        <span className="pill subtle">algoritm {classStats.mostUsedAlgorithm}</span>
                      </div>
                    )}
                    <div className="actions">
                      <button className="btn" onClick={async () => {
                        const res = await fetch(`${CLASSROOM_API}/api/classes/${selectedClass.id}/ai-analysis`, { headers: authHeaders() });
                        if (res.ok) setClassAiAnalysis((await res.json()) as ClassAiAnalysis);
                      }}>Genereaza analiza AI</button>
                      <button className="btn" onClick={() => exportClassReport(selectedClass.id)}>Exporta raport PDF</button>
                    </div>
                    {classAiAnalysis && (
                      <div className="status subtle">
                        {classAiAnalysis.fullAnalysis ? (
                          <pre className="analysis-pre">{classAiAnalysis.fullAnalysis}</pre>
                        ) : (
                          <>
                            <strong>Concluzie:</strong> {classAiAnalysis.conclusion}<br />
                            <strong>Recomandari:</strong> {classAiAnalysis.teacherRecommendations}<br />
                            <strong>Dificultati:</strong> {classAiAnalysis.difficultConcepts}
                          </>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </>
            ) : (
              <div className="notice">Creeaza sau selecteaza o clasa.</div>
            )}
          </div>
        </div>
      </div>
    );
  }

  function renderStudentClassroomPanel() {
    return (
      <div className="card subtle classroom-panel">
        <div className="card-header">
          <div>
            <p className="eyebrow">Clase / Teme</p>
            <h4>Temele mele</h4>
          </div>
          <button className="btn" onClick={loadStudentData} disabled={isClassroomLoading}>Reincarca</button>
        </div>
        <div className="actions wrap">
          <input className="inline-input" placeholder="Cod inscriere" value={joinCode} onChange={(e) => setJoinCode(e.target.value)} />
          <button className="btn btn-primary" onClick={joinClassroom}>Intra in clasa</button>
        </div>
        <div className="class-list compact">
          {studentClasses.map((c) => (
            <button key={c.id} className={`class-item ${selectedStudentClassId === c.id ? 'class-item-active' : ''}`} onClick={() => setSelectedStudentClassId(c.id)}>
              <strong>{c.name}</strong>
              <span>{c.assignmentCount} teme</span>
            </button>
          ))}
        </div>
        <div className="assignment-list">
          {studentAssignments.length === 0 ? <div className="status subtle">Nu exista teme publicate pentru clasa selectata.</div> : studentAssignments.map((a) => {
            const progress = getStudentProgressForAssignment(a.id);
            const displayStatus = getAssignmentDisplayStatus(a, progress);
            const disabled = displayStatus === 'COMPLETED' || displayStatus === 'LATE' || isClassroomLoading;
            return (
              <div className="row-line" key={a.id}>
                <span>{a.title} - {a.algorithm}/{a.direction} - deadline: {formatDueDate(a.dueDate)} - {statusLabel(displayStatus)}</span>
                <button className="btn btn-primary" onClick={() => openAssignmentExecutionPrompt(a)} disabled={disabled}>
                  {displayStatus === 'COMPLETED' ? 'Finalizata' : displayStatus === 'LATE' ? 'Nefacuta la timp' : 'Deschide tema'}
                </button>
              </div>
            );
          })}
        </div>
        {selectedStudentAssignment && <div className="status subtle">Ultima tema rulata: {selectedStudentAssignment.title}</div>}
      </div>
    );
  }

  useEffect(() => {
    if (hasExecution && isPlaying) {
      setHighestReachedStep((prev) => Math.max(prev, currentStep));
    }
  }, [currentStep, hasExecution, isPlaying]);

  useEffect(() => {
    if (!isPlaying || maxSteps === 0) {
      return;
    }
    if (currentStep >= maxSteps - 1) {
      setIsPlaying(false);
      return;
    }
    const timer = setTimeout(() => setCurrentStep((prev) => Math.min(prev + 1, maxSteps - 1)), speedMs);
    return () => clearTimeout(timer);
  }, [isPlaying, maxSteps, currentStep, speedMs]);

  return (
    <div className="page">
      <div className="shell">
        <div className="hero">
          <div>
            <p className="eyebrow">Platforma de invatare</p>
            <h1>Autentificare si vizualizare algoritmi</h1>
            <p className="lede">Toate algoritmii disponibili ruleaza in paralel pe acelasi set de date.</p>
            {token ? (
              <div className="account-bar">
                <span className="pill subtle">Rol: {role}</span>
                <button className="tab" onClick={() => setShowAuthPanel((v) => !v)}>
                  {showAuthPanel ? 'Ascunde auth' : 'Arata auth'}
                </button>
                <button
                  className="tab"
                  onClick={() => {
                    setToken('');
                    setRole('');
                    setShowAuthPanel(true);
                  }}
                >
                  Logout
                </button>
              </div>
            ) : (
              <div className="hero-actions">
                <button className={`tab ${view === 'login' ? 'tab-active' : ''}`} onClick={() => setView('login')}>
                  Login
                </button>
                <button className={`tab ${view === 'register' ? 'tab-active' : ''}`} onClick={() => setView('register')}>
                  Register
                </button>
              </div>
            )}
          </div>
          <div className="hero-glow" aria-hidden />
        </div>
        <div className="content">
          {(!token || showAuthPanel) && (
            <div className="auth-stack">
              {view === 'login' ? (
                <div className="card">
                  <div className="field">
                    <label>Email</label>
                    <input value={loginEmail} onChange={(e) => setLoginEmail(e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Parola</label>
                    <input type="password" value={loginPassword} onChange={(e) => setLoginPassword(e.target.value)} />
                  </div>
                  <div className="actions">
                    <button className="btn btn-primary" onClick={handleLogin} disabled={isLoading}>
                      {isLoading ? 'Se incarca...' : 'Login'}
                    </button>
                  </div>
                </div>
              ) : (
                <div className="card">
                  <div className="grid two-col">
                    <div className="field">
                      <label>Prenume</label>
                      <input value={regFirstName} onChange={(e) => setRegFirstName(e.target.value)} />
                    </div>
                    <div className="field">
                      <label>Nume</label>
                      <input value={regLastName} onChange={(e) => setRegLastName(e.target.value)} />
                    </div>
                  </div>
                  <div className="field">
                    <label>Email</label>
                    <input value={regEmail} onChange={(e) => setRegEmail(e.target.value)} />
                  </div>
                  <div className="grid two-col">
                    <div className="field">
                      <label>Parola</label>
                      <input type="password" value={regPassword} onChange={(e) => setRegPassword(e.target.value)} />
                    </div>
                    <div className="field">
                      <label>Rol</label>
                      <select value={regRole} onChange={(e) => setRegRole(e.target.value as Role)}>
                        <option value="STUDENT">Student</option>
                        <option value="PROFESOR">Profesor</option>
                      </select>
                    </div>
                  </div>
                  <div className="actions">
                    <button className="btn btn-primary" onClick={handleRegister} disabled={isLoading}>
                      {isLoading ? 'Se incarca...' : 'Register'}
                    </button>
                  </div>
                </div>
              )}
              {status && <div className="status">{status}</div>}
            </div>
          )}

          {role === 'ADMIN' ? (
            renderAdminDashboard()
          ) : role === 'PROFESOR' ? (
            renderTeacherDashboard()
          ) : (
          <div className="card highlight">
            <div className="card-header">
              <div>
                <p className="eyebrow">Sorting network</p>
                <h3>Executie single + paralela</h3>
                <p className="muted">Poti rula un singur algoritm (ca inainte) sau toti in paralel, din aceleasi controale.</p>
              </div>
              <div className="pill-row">
                <span className="pill">Algo API: {ALGO_API}</span>
                <span className="pill">Exp API: {EXPLANATION_API}</span>
                {isInspectMode && <span className="pill pill-swap">Inspect mode</span>}
              </div>
            </div>

            {role === 'STUDENT' ? (
              <>
                <div className="student-page-tabs">
                  <button className={`tab ${studentPage === 'classes' ? 'tab-active' : ''}`} onClick={() => setStudentPage('classes')}>
                    Clase si teme
                  </button>
                  <button className={`tab ${studentPage === 'execution' ? 'tab-active' : ''}`} onClick={() => setStudentPage('execution')}>
                    Pagina de executie
                  </button>
                </div>

                {studentPage === 'classes' ? (
                  renderStudentClassroomPanel()
                ) : (
                  <>
                    <div className="card subtle student-execution-banner">
                      <div>
                        <p className="eyebrow">Executie algoritmi</p>
                        <h4>Rulare manuala, din fisier sau pornita din tema</h4>
                        <p className="muted">
                          {selectedStudentAssignment
                            ? `Tema activa: ${selectedStudentAssignment.title} (${selectedStudentAssignment.algorithm}/${selectedStudentAssignment.direction})`
                            : 'Aici ajungi si dupa confirmarea unei teme din pagina "Clase si teme".'}
                        </p>
                        {selectedStudentAssignment && (
                          <p className="hint-line">Dupa rulare poti folosi si butonul "Explica pas", iar execuția intra si in istoric pentru analiza AI.</p>
                        )}
                      </div>
                    </div>

                    <div className="controls-grid">
                      <div className="field">
                        <label>Sir valori</label>
                        <input value={algoValues} onChange={(e) => setAlgoValues(e.target.value)} />
                        <p className="hint-line">Manual: {MIN_VALUES}-{MAX_VALUES} valori.</p>
                      </div>
                      <div className="field">
                        <label>Incarcare fisier</label>
                        <input type="file" accept=".txt,.csv,.dat" onChange={handleFileUpload} />
                        <p className="hint-line">Fisier: {MIN_VALUES}-{MAX_FILE_VALUES} valori.</p>
                        {uploadedFileName && <p className="hint-line file-meta">{uploadedFileName}</p>}
                      </div>
                      <div className="field">
                        <label>Algoritm (mod single)</label>
                        <select value={selectedAlgorithm} onChange={(e) => setSelectedAlgorithm(e.target.value as Algorithm)}>
                          {algorithmOptions()}
                        </select>
                      </div>
                      <div className="field">
                        <label>Directie</label>
                        <select value={direction} onChange={(e) => setDirection(e.target.value as Direction)}>
                          <option value="ASC">ASC</option>
                          <option value="DESC">DESC</option>
                        </select>
                      </div>
                      <div className="field slider-field">
                        <label>Viteza animatie</label>
                        <input
                          type="range"
                          min={150}
                          max={1500}
                          step={50}
                          value={speedMs}
                          onChange={(e) => setSpeedMs(Number(e.target.value))}
                          disabled={!isExecutionStopped && hasExecution}
                        />
                        <div className="slider-hints">
                          <span>incet</span>
                          <span className="hint">{speedMs} ms</span>
                          <span>rapid</span>
                        </div>
                      </div>
                      {maxSteps > 0 && (
                        <div className="field slider-field">
                          <label>Timeline</label>
                          <input
                            type="range"
                            min={0}
                            max={Math.max(inspectLimitStep, 0)}
                            value={currentStep}
                            onChange={(e) => goToStep(Number(e.target.value))}
                            disabled={!canInspectExecution}
                          />
                          <div className="slider-hints">
                            <span>pas</span>
                            <span className="hint">
                              {currentStep + 1}/{maxSteps}
                            </span>
                            <span>pas</span>
                          </div>
                        </div>
                      )}
                    </div>

                    <div className="actions wrap primary-actions">
                      <button
                        className="btn"
                        onClick={() => {
                          const length = Math.floor(Math.random() * (MAX_VALUES - MIN_VALUES + 1)) + MIN_VALUES;
                          setAlgoValues(
                            Array.from(
                              { length },
                              () => Math.floor(Math.random() * (MAX_VALUES - MIN_VALUES + 1)) + MIN_VALUES
                            ).join(',')
                          );
                        }}
                      >
                        Random 2-10
                      </button>
                      <button className="btn btn-primary" onClick={() => runSingleAlgorithm('manual')} disabled={isRunningAlgo}>
                        {isRunningAlgo ? 'Se calculeaza...' : `Ruleaza ${selectedAlgorithm} (manual)`}
                      </button>
                      <button className="btn btn-primary" onClick={() => runSingleAlgorithm('file')} disabled={isRunningAlgo || !hasUploadedValues}>
                        {isRunningAlgo ? 'Se calculeaza...' : `Ruleaza ${selectedAlgorithm} (fisier)`}
                      </button>
                      <button className="btn" onClick={() => runAllAlgorithms('manual')} disabled={isRunningAlgo}>
                        {isRunningAlgo ? 'Se calculeaza...' : 'Ruleaza toti (manual)'}
                      </button>
                      <button className="btn" onClick={() => runAllAlgorithms('file')} disabled={isRunningAlgo || !hasUploadedValues}>
                        {isRunningAlgo ? 'Se calculeaza...' : 'Ruleaza toti (fisier)'}
                      </button>
                      <button className="btn" onClick={stopExecution} disabled={!isPlaying}>
                        Stop
                      </button>
                      {isStoppedManually && highestReachedStep < Math.max(maxSteps - 1, 0) && (
                        <button className="btn" onClick={resumeExecution}>
                          Resume
                        </button>
                      )}
                      <button className="btn" onClick={resetExecution} disabled={!isExecutionStopped}>
                        Reset
                      </button>
                    </div>

                    <div className="workspace-grid">
                      <div className="algo-list">
                        {visibleAlgorithms.map((algorithm) => {
                          const run = runs[algorithm];
                          const isSelected = selectedViewAlgorithm === algorithm;
                          const stepCount = run.steps.length;
                          const current = stepCount === 0 ? 0 : Math.min(currentStep + 1, stepCount);
                          return (
                            <button
                              key={algorithm}
                              type="button"
                              className={`algo-list-item ${isSelected ? 'algo-list-item-active' : ''}`}
                              onClick={() => setSelectedViewAlgorithm(algorithm)}
                            >
                              <div className="algo-list-top">
                                <span className="algo-name" title={algorithm}>{ALGORITHM_LABELS[algorithm]}</span>
                                <span className="pill subtle">
                                  {current}/{stepCount}
                                </span>
                              </div>
                              <div className="algo-list-meta">
                                <span className="pill subtle">swaps: {run.metrics?.totalSwaps ?? 0}</span>
                                <span className="pill subtle">{formatDurationMs(run.metrics?.executionTimeMs ?? 0)} ms</span>
                              </div>
                            </button>
                          );
                        })}
                      </div>

                      <div className="algo-detail">
                        {(() => {
                          const algorithm = selectedViewAlgorithm;
                          const run = runs[algorithm];
                          const stepCount = run.steps.length;
                          const runInspectLimit = stepCount > 0
                            ? (canInspectExecution ? Math.min(highestReachedStep, stepCount - 1) : stepCount - 1)
                            : 0;
                          const runFinishedWhileOthersRun = isPlaying && stepCount > 0 && currentStep >= stepCount - 1;
                          const canInspectThisRun = canInspectExecution || runFinishedWhileOthersRun;
                          const fallbackStepIndex = stepCount > 0 ? Math.min(currentStep, stepCount - 1) : 0;
                          const displayedStepIndex = stepCount > 0
                            ? Math.min(inspectStepByAlgorithm[algorithm] ?? fallbackStepIndex, runInspectLimit)
                            : 0;
                          const activeStep = stepCount > 0 ? run.steps[displayedStepIndex] : null;
                          const activeArrayState = activeStep?.arrayState ?? sourceValues;
                          const stageBuckets = buildStageBuckets(run.steps);
                          return (
                            <div className="card subtle">
                              <div className="card-header tight">
                                <div>
                                  <p className="eyebrow">Algoritm selectat</p>
                                  <h4>{ALGORITHM_LABELS[algorithm]}</h4>
                                </div>
                                {run.metrics && (
                                  <div className="metrics">
                                    <span className="pill subtle">pasi: {run.metrics.totalSteps}</span>
                                    <span className="pill subtle">swaps: {run.metrics.totalSwaps}</span>
                                    <span className="pill subtle">{formatDurationMs(run.metrics.executionTimeMs)} ms</span>
                                  </div>
                                )}
                              </div>

                              <div className="visuals">
                                <div className="chart-card">
                                  <BarChart
                                    values={activeArrayState}
                                    activeIndices={activeStep ? [activeStep.leftIndex, activeStep.rightIndex] : []}
                                    swapped={!!activeStep?.swapped}
                                  />
                                </div>
                                <div className="chart-card">
                                  {activeArrayState.length > MAX_VALUES ? (
                                    <LargeDatasetView
                                      values={activeArrayState}
                                      activeIndices={activeStep ? [activeStep.leftIndex, activeStep.rightIndex] : []}
                                    />
                                  ) : (
                                    <NetworkView
                                      length={activeArrayState.length}
                                      stages={stageBuckets}
                                      currentStepIndex={displayedStepIndex}
                                      onSelectStep={(idx) => goToAlgorithmStep(algorithm, idx, runInspectLimit)}
                                    />
                                  )}
                                </div>
                              </div>

                              {activeStep && (
                                <div className="inspect-layout">
                                  <div className="card subtle">
                                    <div className="card-header tight">
                                      <div>
                                        <p className="eyebrow">Comparator activ</p>
                                        <h4>{activeStep.leftIndex} vs {activeStep.rightIndex}</h4>
                                      </div>
                                      <span className={`pill ${activeStep.swapped ? 'pill-swap' : 'pill-noswap'}`}>
                                        {activeStep.swapped ? 'SWAP' : 'NO SWAP'}
                                      </span>
                                    </div>
                                    <div className="actions wrap">
                                      <button
                                        className="btn"
                                        onClick={() => goToAlgorithmStep(algorithm, displayedStepIndex - 1, runInspectLimit)}
                                        disabled={!canInspectThisRun || displayedStepIndex <= 0}
                                      >
                                        Pas anterior
                                      </button>
                                      {canInspectThisRun && displayedStepIndex < runInspectLimit && (
                                        <button className="btn" onClick={() => goToAlgorithmStep(algorithm, displayedStepIndex + 1, runInspectLimit)}>
                                          Pas urmator
                                        </button>
                                      )}
                                      <button
                                        className="btn btn-primary"
                                        onClick={() => askExplanation(algorithm, activeStep, activeArrayState, displayedStepIndex)}
                                        disabled={run.isExplaining || !canInspectThisRun}
                                      >
                                        {run.isExplaining ? 'Se genereaza...' : 'Explica pas'}
                                      </button>
                                    </div>
                                    {inspectNotice && <div className="status subtle">{inspectNotice}</div>}
                                    {run.explainStatus && <div className="status">{run.explainStatus}</div>}
                                    <div className="status subtle">
                                      {activeStep.explanation ?? 'Explicatia pasului va aparea aici dupa ce apesi "Explica pas".'}
                                    </div>
                                  </div>
                                </div>
                              )}
                            </div>
                          );
                        })()}
                      </div>
                    </div>

                    <div className="card subtle history-card">
                      <div className="card-header tight">
                        <div>
                          <p className="eyebrow">Istoric comparatii</p>
                          <h4>Timpi si pasi pe rulare</h4>
                        </div>
                      </div>

                      <div className="history-ai-zones">
                        <div className="history-ai-zone">
                          <p className="eyebrow">Regula single</p>
                          <p className="hint-line">Rularile individuale raman in istoric pana cand apesi Curata istoric.</p>
                        </div>
                        <div className="history-ai-zone">
                          <p className="eyebrow">Regula parallel</p>
                          <p className="hint-line">Cand rulezi toti algoritmii, istoricul se curata si ramane doar setul nou.</p>
                        </div>
                      </div>

                      <div className="history-sections">
                        <div className="history-section">
                          <div className="card-header tight">
                            <div>
                              <p className="eyebrow">Single</p>
                              <h4>Rulari individuale (manual + fisier)</h4>
                            </div>
                            <button
                              className="btn"
                              onClick={() => {
                                setHistory((prev) => prev.filter((h) => h.mode !== 'single'));
                                if (history.some((h) => h.id === activeHistoryEntryId && h.mode === 'single')) {
                                  setActiveHistoryEntryId(null);
                                }
                              }}
                              disabled={singleHistory.length === 0}
                            >
                              Curata istoric single
                            </button>
                          </div>
                          {singleHistory.length === 0 ? (
                            <div className="status subtle">Nu exista inca rulari individuale.</div>
                          ) : (
                            <div className="history-list">
                              {singleHistory.map((entry) => renderHistoryEntry(entry, false))}
                            </div>
                          )}
                        </div>

                        <div className="history-section">
                          <div className="card-header tight">
                            <div>
                              <p className="eyebrow">Parallel</p>
                              <h4>Rulare toti algoritmii (manual + fisier)</h4>
                            </div>
                            <button
                              className="btn"
                              onClick={() => {
                                setHistory((prev) => prev.filter((h) => h.mode !== 'parallel'));
                                if (history.some((h) => h.id === activeHistoryEntryId && h.mode === 'parallel')) {
                                  setActiveHistoryEntryId(null);
                                }
                              }}
                              disabled={parallelHistory.length === 0}
                            >
                              Curata istoric parallel
                            </button>
                          </div>
                          {parallelHistory.length === 0 ? (
                            <div className="status subtle">Nu exista inca rulari cu toti algoritmii.</div>
                          ) : (
                            <div className="history-list">
                              {parallelHistory.map((entry) => renderHistoryEntry(entry, true))}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </>
                )}
              </>
            ) : (
              <div className="notice">Logheaza-te ca STUDENT pentru rulare.</div>
            )}
          </div>
          )}
        </div>
      </div>
      {pendingStudentAssignment && (
        <div className="modal-backdrop" onClick={closeAssignmentExecutionPrompt}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <p className="eyebrow">Confirmare tema</p>
            <h3>Continui executia pe pagina de executie?</h3>
            <p className="muted">
              Tema <strong>{pendingStudentAssignment.title}</strong> va deschide pagina de executie si va porni algoritmul
              {' '}
              <strong>{pendingStudentAssignment.algorithm}</strong> pe setul de date al temei.
            </p>
            <div className="actions">
              <button className="btn btn-primary" onClick={confirmAssignmentExecution} disabled={isClassroomLoading || isRunningAlgo}>
                Da, continua
              </button>
              <button className="btn" onClick={closeAssignmentExecutionPrompt}>
                Nu, inchide
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

type BarChartProps = {
  values: number[];
  activeIndices: number[];
  swapped: boolean;
};

function BarChart({ values, activeIndices, swapped }: BarChartProps) {
  const max = Math.max(...values, 1);
  const dense = values.length > 20;
  const width = Math.max(values.length * (dense ? 18 : 50), 320);
  const barWidth = Math.max(dense ? 6 : 20, width / (values.length * (dense ? 1.2 : 1.4)));
  const indexStep = Math.max(1, Math.floor(values.length / 10));

  return (
    <div className="chart-scroll">
      <svg className="barchart" width={width} viewBox={`0 0 ${width} 240`} preserveAspectRatio="xMidYMid meet">
        {values.map((v, idx) => {
          const h = (v / max) * 200;
          const x = idx * (width / values.length);
          const isActive = activeIndices.includes(idx);
          const showIndex = !dense || idx % indexStep === 0 || idx === values.length - 1;
          return (
            <g key={idx} transform={`translate(${x + 10}, ${220 - h})`}>
              <rect width={barWidth} height={h} rx={dense ? 3 : 6} className={`bar ${isActive ? (swapped ? 'bar-swap' : 'bar-active') : ''}`} />
              {!dense && (
                <text x={barWidth / 2} y={-6} textAnchor="middle" className="bar-label">
                  {v}
                </text>
              )}
              {showIndex && (
                <text x={barWidth / 2} y={h + 16} textAnchor="middle" className="bar-index">
                  {idx}
                </text>
              )}
            </g>
          );
        })}
      </svg>
    </div>
  );
}

type LargeDatasetViewProps = {
  values: number[];
  activeIndices: number[];
};

function LargeDatasetView({ values, activeIndices }: LargeDatasetViewProps) {
  const sorted = [...values].sort((a, b) => a - b);
  const min = sorted[0] ?? 0;
  const max = sorted[sorted.length - 1] ?? 0;
  const avg = values.length === 0 ? 0 : values.reduce((acc, val) => acc + val, 0) / values.length;
  const median =
    sorted.length === 0
      ? 0
      : sorted.length % 2 === 0
      ? ((sorted[sorted.length / 2 - 1] ?? 0) + (sorted[sorted.length / 2] ?? 0)) / 2
      : (sorted[Math.floor(sorted.length / 2)] ?? 0);
  const range = Math.max(max - min, 1);
  const lineWidth = Math.max(values.length * 10, 460);
  const binsCount = 20;
  const bins = Array.from({ length: binsCount }, () => 0);
  values.forEach((value) => {
    const normalized = (value - min) / range;
    const idx = Math.min(binsCount - 1, Math.floor(normalized * binsCount));
    bins[idx] += 1;
  });
  const maxBin = Math.max(...bins, 1);
  const points = values
    .map((v, idx) => {
      const x = values.length === 1 ? 20 : 20 + (idx / (values.length - 1)) * (lineWidth - 40);
      const y = 170 - ((v - min) / range) * 130;
      return `${x},${y}`;
    })
    .join(' ');

  return (
    <div className="large-dataset">
      <div className="metrics">
        <span className="pill subtle">valori: {values.length}</span>
        <span className="pill subtle">min: {min}</span>
        <span className="pill subtle">max: {max}</span>
        <span className="pill subtle">medie: {avg.toFixed(2)}</span>
        <span className="pill subtle">mediana: {median.toFixed(2)}</span>
      </div>
      <div className="chart-scroll">
        <svg className="barchart" width={lineWidth} viewBox={`0 0 ${lineWidth} 190`} preserveAspectRatio="xMidYMid meet">
          <polyline points={points} className="trend-line" />
          {activeIndices.map((idx) => {
            if (idx < 0 || idx >= values.length) {
              return null;
            }
            const x = values.length === 1 ? 20 : 20 + (idx / (values.length - 1)) * (lineWidth - 40);
            const y = 170 - ((values[idx] - min) / range) * 130;
            return <circle key={`active-${idx}`} cx={x} cy={y} r={4} className="trend-active" />;
          })}
        </svg>
      </div>
      <div className="histogram">
        {bins.map((count, idx) => (
          <div key={idx} className="hist-col" title={`Bucket ${idx + 1}: ${count}`}>
            <div className="hist-bar" style={{ height: `${(count / maxBin) * 100}%` }} />
          </div>
        ))}
      </div>
    </div>
  );
}

type NetworkViewProps = {
  length: number;
  stages: NetworkStage[];
  currentStepIndex: number;
  onSelectStep?: (stepIndex: number) => void;
};

function NetworkView({ length, stages, currentStepIndex, onSelectStep }: NetworkViewProps) {
  const rowGap = 26;
  const stageGap = 90;
  const height = length * rowGap + 40;
  const width = Math.max(stages.length * stageGap + 80, 240);

  return (
    <svg className="network" viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="xMidYMid meet">
      {Array.from({ length }).map((_, idx) => (
        <line
          key={`lane-${idx}`}
          x1={20}
          x2={width - 20}
          y1={20 + idx * rowGap}
          y2={20 + idx * rowGap}
          className="lane"
        />
      ))}
      {stages.map((stage, stageIdx) => {
        const x = 40 + stageIdx * stageGap;
        return stage.list.map((step) => {
          const y1 = 20 + step.leftIndex * rowGap;
          const y2 = 20 + step.rightIndex * rowGap;
          const isActive = step.stepIndex === currentStepIndex;
          const isDone = step.stepIndex < currentStepIndex;
          return (
            <g key={step.stepIndex} onClick={() => onSelectStep?.(step.stepIndex)} className="clickable">
              <line x1={x} x2={x} y1={y1} y2={y2} className={`comparator ${isActive ? 'cmp-active' : isDone ? 'cmp-done' : ''}`} />
              <circle cx={x} cy={y1} r={5} className={`node ${isActive ? 'node-active' : ''}`} />
              <circle cx={x} cy={y2} r={5} className={`node ${isActive ? 'node-active' : ''}`} />
            </g>
          );
        });
      })}
    </svg>
  );
}

export default App;
