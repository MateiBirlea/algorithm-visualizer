import { buildStageBuckets, isPowerOfTwo, validateRunInput } from './App';

test('blocks BITONIC execution for invalid sizes before animation can start', () => {
  const message = validateRunInput([6, 4, 3, 2, 1, 5], 10, 'manual', 'BITONIC');

  expect(message).toContain('Bitonic Sort poate fi executat numai');
});

test('allows BITONIC execution for a valid power-of-two size', () => {
  const message = validateRunInput([7, 2, 9, 1, 5, 3, 8, 4], 10, 'manual', 'BITONIC');

  expect(message).toBe('');
  expect(isPowerOfTwo(8)).toBe(true);
  expect(isPowerOfTwo(6)).toBe(false);
});

test('blocks BATCHER execution for invalid sizes before animation can start', () => {
  const message = validateRunInput([7, 5, 3, 1, 2, 4, 6], 10, 'manual', 'BATCHER_ODD_EVEN_MERGE_SORT');

  expect(message).toContain('Batcher Odd-Even Merge Sort poate fi executat doar');
});

test('keeps non-adjacent BITONIC comparators in the displayed network data', () => {
  const stages = buildStageBuckets([
    {
      stepIndex: 12,
      stageIndex: 0,
      leftIndex: 0,
      rightIndex: 4,
      leftValue: 1,
      rightValue: 8,
      swapped: false,
      arrayState: [1, 2, 7, 9, 8, 5, 4, 3],
      comparatorDirection: 'ASC',
      phaseName: 'BITONIC_MERGE',
    },
    {
      stepIndex: 16,
      stageIndex: 1,
      leftIndex: 0,
      rightIndex: 2,
      leftValue: 1,
      rightValue: 4,
      swapped: false,
      arrayState: [1, 2, 4, 3, 8, 5, 7, 9],
      comparatorDirection: 'ASC',
      phaseName: 'BITONIC_MERGE',
    },
  ]);

  expect(stages[0].list[0].leftIndex).toBe(0);
  expect(stages[0].list[0].rightIndex).toBe(4);
  expect(stages[1].list[0].leftIndex).toBe(0);
  expect(stages[1].list[0].rightIndex).toBe(2);
});
