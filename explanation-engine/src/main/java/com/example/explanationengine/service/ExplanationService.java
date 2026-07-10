package com.example.explanationengine.service;

import java.util.stream.Collectors;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.explanationengine.dto.AlgorithmRunResultDto;
import com.example.explanationengine.config.OllamaProperties;
import com.example.explanationengine.dto.ClassroomAnalysisRequest;
import com.example.explanationengine.dto.ComparativeAnalysisRequestDto;
import com.example.explanationengine.dto.ExplanationRequest;
import com.example.explanationengine.dto.ExplanationResponse;
import com.example.explanationengine.dto.RunAnalysisRequest;
import com.example.explanationengine.dto.RunAnalysisResultDto;
import com.example.explanationengine.dto.SingleAlgorithmAnalysisRequestDto;

@Service
public class ExplanationService {

    private static final Logger log = LoggerFactory.getLogger(ExplanationService.class);

    private final OllamaClient ollamaClient;
    private final OllamaProperties properties;

    public ExplanationService(OllamaClient ollamaClient, OllamaProperties properties) {
        this.ollamaClient = ollamaClient;
        this.properties = properties;
    }

    public ExplanationResponse generate(ExplanationRequest request) {
        String userPrompt = buildPrompt(request);
        long start = System.currentTimeMillis();
        String reply = ollamaClient.chat(userPrompt);
        long end = System.currentTimeMillis();
        return new ExplanationResponse(reply, properties.model(), end - start);
    }

    public ExplanationResponse analyzeRun(RunAnalysisRequest request) {
        String userPrompt = buildRunAnalysisPrompt(request);
        long start = System.currentTimeMillis();
        String reply = ollamaClient.chat(userPrompt);
        long end = System.currentTimeMillis();
        return new ExplanationResponse(reply, properties.model(), end - start);
    }

    public ExplanationResponse analyzeSingleAlgorithm(SingleAlgorithmAnalysisRequestDto request) {
        String userPrompt = buildSingleAlgorithmAnalysisPrompt(request);
        long start = System.currentTimeMillis();
        String reply = ollamaClient.chat(userPrompt);
        long end = System.currentTimeMillis();
        return new ExplanationResponse(formatSingleAlgorithmAnalysisResponse(request, reply), properties.model(), end - start);
    }

    public ExplanationResponse analyzeComparative(ComparativeAnalysisRequestDto request) {
        log.info("ComparativeAnalysisRequestDto.results.size()={}", request.getResults().size());
        request.getResults().forEach(result ->
                log.info("Comparative result: algorithmName={}, executedAs={}, totalSteps={}, totalComparisons={}, totalSwaps={}",
                        result.getAlgorithmName(),
                        result.getExecutedAs(),
                        result.getTotalSteps(),
                        result.getTotalComparisons(),
                        result.getTotalSwaps())
        );

        String userPrompt = buildComparativeAnalysisPrompt(request);
        long start = System.currentTimeMillis();
        String reply = ollamaClient.chat(userPrompt);
        long end = System.currentTimeMillis();
        return new ExplanationResponse(formatComparativeAnalysisResponse(request, reply), properties.model(), end - start);
    }

    public ExplanationResponse analyzeClassroom(ClassroomAnalysisRequest request) {
        String userPrompt = buildClassroomAnalysisPrompt(request);
        long start = System.currentTimeMillis();
        String reply = ollamaClient.chat(userPrompt);
        long end = System.currentTimeMillis();
        return new ExplanationResponse(reply, properties.model(), end - start);
    }

    private String buildPrompt(ExplanationRequest req) {
        String algorithmName = valueOr(req.getAlgorithmName(), req.getAlgorithm());
        String sortDirection = valueOr(req.getSortDirection(), req.getDirection());
        String comparatorDirection = valueOr(req.getComparatorDirection(), sortDirection);
        String phaseName = valueOr(req.getPhaseName(), req.getStageName(), "N/A");
        List<Integer> comparedIndices = listOr(req.getComparedIndices(), List.of(req.getLeftIndex(), req.getRightIndex()));
        List<Integer> comparedBefore = listOr(req.getComparedValuesBefore(), List.of(req.getLeftValue(), req.getRightValue()));
        List<Integer> afterArray = listOr(req.getArrayAfterStep(), req.getAfterArrayState(), req.getArrayState());
        List<Integer> beforeArray = listOr(req.getArrayBeforeStep(), req.getBeforeArrayState(), req.getArrayState());
        List<Integer> comparedAfter = listOr(req.getComparedValuesAfter(), valuesAt(afterArray, req.getLeftIndex(), req.getRightIndex()));
        boolean didSwap = Boolean.TRUE.equals(req.getDidSwap() == null ? req.getSwapped() : req.getDidSwap());
        boolean finalStep = Boolean.TRUE.equals(req.getIsFinalStep());
        boolean globallySortedBefore = Boolean.TRUE.equals(req.getIsArrayGloballySortedBeforeStep());
        boolean globallySorted = Boolean.TRUE.equals(req.getIsArrayGloballySortedAfterStep());
        Integer comparatorDistance = req.getComparatorDistance() == null
                ? Math.abs(req.getRightIndex() - req.getLeftIndex())
                : req.getComparatorDistance();
        Integer mergeSize = req.getMergeSize() == null ? req.getBitonicSequenceSize() : req.getMergeSize();
        String oddEvenPhase = valueOr(req.getOddEvenPhase(), phaseName.contains("ODD") ? "ODD" : phaseName.contains("EVEN") ? "EVEN" : "");
        String explanationRules = req.getExplanationRules() == null || req.getExplanationRules().isEmpty()
                ? defaultExplanationRules()
                : req.getExplanationRules().stream().map(rule -> "- " + rule).collect(Collectors.joining("\n"));
        String warning = valueOr(
                req.getExplanationWarning(),
                "Vectorul poate fi intr-o stare intermediara si nu trebuie descris ca sortat global decat daca isArrayGloballySortedAfterStep=true."
        );
        String beforeState = join(beforeArray);
        String afterState = join(afterArray);
        return """
                Esti tutor pentru vizualizarea unui algoritm de sortare. Explica DOAR comparatorul curent.

                DTO pas curent:
                - algorithmName: %s
                - sortDirection: %s
                - stepIndex: %d
                - totalSteps: %s
                - comparedIndices: %s
                - comparedValuesBefore: %s
                - comparedValuesAfter: %s
                - didSwap: %s
                - arrayBeforeStep: [%s]
                - arrayAfterStep: [%s]
                - comparatorDirection: %s
                - comparatorDistance: %d
                - phaseName: %s
                - bitonicSequenceSize: %s
                - mergeSize: %s
                - networkStage: %s
                - networkSubStage: %s
                - isBuildingBitonicSequence: %s
                - isBitonicMergeStep: %s
                - oddEvenPhase: %s
                - passNumber: %s
                - isFinalStep: %s
                - isArrayGloballySortedBeforeStep: %s
                - isArrayGloballySortedAfterStep: %s
                - explanationWarning: %s

                Reguli obligatorii:
                %s

                Format dorit:
                - Raspunde in romana, in 2-4 propozitii scurte.
                - Incepe cu "Comparatorul curent conecteaza pozitiile ...".
                - Pentru BITONIC, spune ca pozitiile sunt conectate de reteaua Bitonic in phaseName si mentioneaza comparatorDistance si mergeSize.
                - Pentru ODD_EVEN, mentioneaza faza oddEvenPhase si passNumber.
                - Pentru BATCHER_ODD_EVEN_MERGE_SORT, mentioneaza reteaua odd-even merge si mergeSize.
                - Pentru PAIRWISE_SORTING_NETWORK, mentioneaza etapa pairwise, networkStage si networkSubStage; daca phaseName contine NAIVE, spune explicit ca pasul apartine variantei naive implementate.
                - Pentru BUBBLE_SORTING_NETWORK, mentioneaza passNumber si faptul ca este o comparatie locala intre vecini.
                - Incheie cu avertizarea despre sortarea globala daca isArrayGloballySortedAfterStep=false.

                """.formatted(
                algorithmName,
                sortDirection,
                req.getStepIndex(),
                req.getTotalSteps() == null ? "N/A" : req.getTotalSteps().toString(),
                comparedIndices,
                comparedBefore,
                comparedAfter,
                didSwap,
                beforeState,
                afterState,
                comparatorDirection,
                comparatorDistance,
                phaseName,
                req.getBitonicSequenceSize() == null ? "N/A" : req.getBitonicSequenceSize().toString(),
                mergeSize == null ? "N/A" : mergeSize.toString(),
                req.getNetworkStage() == null ? "N/A" : req.getNetworkStage().toString(),
                req.getNetworkSubStage() == null ? "N/A" : req.getNetworkSubStage().toString(),
                Boolean.TRUE.equals(req.getIsBuildingBitonicSequence()),
                Boolean.TRUE.equals(req.getIsBitonicMergeStep()),
                oddEvenPhase.isBlank() ? "N/A" : oddEvenPhase,
                req.getPassNumber() == null ? "N/A" : req.getPassNumber().toString(),
                finalStep,
                globallySortedBefore,
                globallySorted,
                warning,
                explanationRules
        );
    }

    private String join(List<Integer> values) {
        return values.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    @SafeVarargs
    private final List<Integer> listOr(List<Integer>... candidates) {
        for (List<Integer> candidate : candidates) {
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }
        return List.of();
    }

    private String valueOr(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return "";
    }

    private List<Integer> valuesAt(List<Integer> values, Integer leftIndex, Integer rightIndex) {
        if (values == null || leftIndex == null || rightIndex == null || leftIndex < 0 || rightIndex < 0
                || leftIndex >= values.size() || rightIndex >= values.size()) {
            return List.of();
        }
        return List.of(values.get(leftIndex), values.get(rightIndex));
    }

    private String defaultExplanationRules() {
        return """
                - Explica doar comparatorul curent.
                - Nu spune ca vectorul este sortat global decat daca isArrayGloballySortedAfterStep=true.
                - Pentru Bitonic, explica faptul ca pozitiile sunt comparate deoarece fac parte din reteaua Bitonic, nu pentru ca indexul mai mic trebuie mereu sa aiba valoarea mai mica.
                - Pentru Bitonic, mentioneaza comparatorDistance, mergeSize si phaseName.
                - Pentru Odd-Even, mentioneaza daca este faza EVEN sau ODD.
                - Pentru Batcher Odd-Even Merge Sort, mentioneaza reteaua odd-even merge.
                - Pentru Pairwise Sorting Network, mentioneaza etapa pairwise si networkStage/networkSubStage; daca phaseName contine NAIVE, mentioneaza ca este varianta naive implementata.
                - Pentru Bubble Sorting Network, mentioneaza passNumber si comparatia locala dintre vecini.
                - Daca didSwap=true, explica exact de ce s-a facut swap conform directiei comparatorului.
                - Daca didSwap=false, explica exact de ce nu s-a facut swap.
                - Nu inventa justificari despre alte portiuni ale vectorului daca nu sunt in DTO.
                - Foloseste doar valorile din DTO.
                """;
    }

    private String buildSingleAlgorithmAnalysisPrompt(SingleAlgorithmAnalysisRequestDto req) {
        RunAnalysisResultDto result = req.getResult();
        String inputValues = req.getInputArray().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        return """
                Analizeaza strict statisticile rularii curente ale unui singur algoritm de sortare.

                Context rulare:
                - Sursa date: %s
                - Directie: %s
                - Numar valori: %d
                - Vector intrare: [%s]

                Rezultat:
                %s

                Cerinte raspuns:
                - Raspunde STRICT in formatul de mai jos, cate o singura linie per camp.
                - Fara markdown, fara bold, fara comparatii cu alti algoritmi.
                - Observatiile si concluziile trebuie bazate exclusiv pe datele din Rezultat.
                - Nu genera recomandari generale despre algoritm.
                - Nu spune ca algoritmul este potrivit pentru seturi mici.
                - Afiseaza separat COMPARATII_TEORETICE si COMPARATII_EXECUTATE, cu valorile numerice.
                - Daca comparisonMatch=true, STATUS trebuie sa fie "Coincid ✓"; daca comparisonMatch=false, STATUS trebuie sa fie "Difera ✗".

                FORMAT OBLIGATORIU:
                ALGORITM: <nume algoritm>
                TIMP_MS: <numar>
                PASI_TOTALI: <numar>
                SWAPURI_EXECUTATE: <numar swap-uri necesare>
                SORTARE_CORECTA: <da/nu + referire la correctlySorted/finalArray>
                COMPARATII_TEORETICE: <valoarea theoreticalComparisons>
                COMPARATII_EXECUTATE: <valoarea actualComparisons>
                STATUS: <Coincid ✓ daca comparisonMatch=true, Difera ✗ daca comparisonMatch=false>
                CONCLUZIE_STATISTICA: <ce inseamna verificarea dintre teoretic si executat, strict din statisticile rularii curente>
                """.formatted(
                req.getSource(),
                req.getSortDirection(),
                req.getElementCount(),
                inputValues,
                toResultLine(result)
        );
    }

    private String buildComparativeAnalysisPrompt(ComparativeAnalysisRequestDto req) {
        String inputValues = req.getInputArray().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        String resultLines = req.getResults().stream()
                .map(this::toComparativeResultLine)
                .collect(Collectors.joining("\n"));
        AlgorithmRunResultDto comparisonWinner = minByComparisons(req.getResults());
        AlgorithmRunResultDto swapWinner = minBySwaps(req.getResults());
        AlgorithmRunResultDto comparisonLoser = maxByComparisons(req.getResults());
        int maxComparisons = maxComparisons(req.getResults());
        int minComparisons = minComparisons(req.getResults());
        int maxSwaps = maxSwaps(req.getResults());
        int minSwaps = minSwaps(req.getResults());
        double comparisonDiffPercent = percentDiff(maxComparisons(req.getResults()), minComparisons(req.getResults()));
        double swapDiffPercent = percentDiff(maxSwaps(req.getResults()), minSwaps(req.getResults()));
        String algorithmNames = req.getResults().stream()
                .map(AlgorithmRunResultDto::getAlgorithmName)
                .collect(Collectors.joining(", "));
        String sortStatusLine = comparativeMetricLine(req.getResults(), r -> Boolean.TRUE.equals(r.getCorrectlySorted()) ? "DA" : "NU");
        String comparisonLine = comparativeMetricLine(req.getResults(), r -> r.getTotalComparisons().toString());
        String swapLine = comparativeMetricLine(req.getResults(), r -> r.getTotalSwaps().toString());
        String timeLine = comparativeMetricLine(req.getResults(), r -> r.getExecutionTimeMs().toString());

        return """
                Analizeaza comparativ TOATE rezultatele din lista. Acesta NU este DTO de analiza single.

                Context comparativ:
                - elementCount: %d
                - sortDirection: %s
                - inputArray: [%s]
                - results.size: %d

                Rezultate primite:
                %s

                Calcule comparative deja verificate:
                - castigatorComparatii: %s
                - castigatorSwapuri: %s
                - celeMaiMulteComparatii: %s
                - diferentaComparatiiProcente: %.2f%%
                - diferentaSwapuriProcente: %.2f%%

                Valori obligatorii de copiat in raspuns:
                - ALGORITMI_ANALIZATI: %s
                - SORTARE_CORECTA: %s
                - COMPARATII: %s
                - SWAP_URI: %s
                - TIMP_MS: %s
                - CASTIGATOR_COMPARATII: %s
                - CASTIGATOR_SWAP_URI: %s
                - CELE_MAI_MULTE_COMPARATII: %s = %d
                - DIFERENTA_COMPARATII: (%d - %d) / %d * 100 = %.2f%%
                - DIFERENTA_SWAP_URI: (%d - %d) / %d * 100 = %.2f%%

                Reguli obligatorii:
                - Raspunde fara markdown, fara bold si fara liste.
                - Raspunde doar cu linia CONCLUZIE_COMPARATIVA; valorile numerice vor fi formatate de aplicatie din DTO.
                - Foloseste TOATE elementele din results; nu folosi doar primul rezultat.
                - Nu afisa campuri globale de tip PASI_TOTALI=492 sau SWAPURI_EXECUTATE=212.
                - Pentru fiecare metrica, afiseaza valoarea pe fiecare algoritm: BITONIC = X, ODD_EVEN = Y.
                - Concluzia trebuie sa compare algoritmii intre ei si sa fie bazata strict pe results.
                - Nu inventa informatii despre algoritmi, distributia datelor sau recomandari generale.
                - Nu spune ca un algoritm este eficient la comparatii daca totalComparisons este mai mare decat la alt algoritm.
                - Castigatorul la comparatii este exclusiv algoritmul cu totalComparisons minim.
                - Castigatorul la swap-uri este exclusiv algoritmul cu totalSwaps minim.
                - Mentioneaza explicit algoritmul cu cele mai multe comparatii.
                - Daca PAIRWISE_SORTING_NETWORK are valoarea maxima la comparatii, spune ca este cel mai slab la criteriul comparatii pentru aceasta rulare.
                - Daca algorithmName este diferit de executedAs, mentioneaza explicit ca rezultatul nu este valid comparativ.

                FORMAT OBLIGATORIU:
                ALGORITMI_ANALIZATI: <numele tuturor algoritmilor din results>
                SORTARE_CORECTA: <ALGORITM = DA/NU pentru fiecare rezultat>
                COMPARATII: <ALGORITM = totalComparisons pentru fiecare rezultat>
                SWAP_URI: <ALGORITM = totalSwaps pentru fiecare rezultat>
                TIMP_MS: <ALGORITM = executionTimeMs pentru fiecare rezultat>
                CASTIGATOR_COMPARATII: <algoritmul cu cele mai putine comparatii>
                CASTIGATOR_SWAP_URI: <algoritmul cu cele mai putine swap-uri>
                CELE_MAI_MULTE_COMPARATII: <algoritmul cu cele mai multe comparatii si valoarea>
                DIFERENTA_COMPARATII: <formula (max-min)/max*100 si procentul calculat>
                DIFERENTA_SWAP_URI: <formula (max-min)/max*100 si procentul calculat>
                CONCLUZIE_COMPARATIVA: <concluzie bazata pe toate rezultatele din lista>
                """.formatted(
                req.getElementCount(),
                req.getSortDirection(),
                inputValues,
                req.getResults().size(),
                resultLines,
                comparisonWinner == null ? "N/A" : comparisonWinner.getAlgorithmName(),
                swapWinner == null ? "N/A" : swapWinner.getAlgorithmName(),
                comparisonLoser == null ? "N/A" : comparisonLoser.getAlgorithmName(),
                comparisonDiffPercent,
                swapDiffPercent,
                algorithmNames,
                sortStatusLine,
                comparisonLine,
                swapLine,
                timeLine,
                comparisonWinner == null ? "N/A" : comparisonWinner.getAlgorithmName(),
                swapWinner == null ? "N/A" : swapWinner.getAlgorithmName(),
                comparisonLoser == null ? "N/A" : comparisonLoser.getAlgorithmName(),
                maxComparisons,
                maxComparisons,
                minComparisons,
                maxComparisons,
                comparisonDiffPercent,
                maxSwaps,
                minSwaps,
                maxSwaps,
                swapDiffPercent
        );
    }

    private String buildRunAnalysisPrompt(RunAnalysisRequest req) {
        String inputValues = req.getInputValues().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String resultLines = req.getResults().stream()
                .map(this::toResultLine)
                .collect(Collectors.joining("\n"));

        boolean comparative = req.getResults().size() > 1 || "parallel".equalsIgnoreCase(req.getMode());

        if (comparative) {
            return """
                Analizeaza strict statisticile rularilor curente ale algoritmilor de sortare.

                Context rulare:
                - Mod: %s
                - Sursa date: %s
                - Directie: %s
                - Numar valori: %d
                - Vector intrare: [%s]

                Rezultate:
                %s

                Cerinte raspuns:
                - Raspunde STRICT in formatul de mai jos, cate o singura linie per camp.
                - Fara markdown, fara bold, fara liste numerotate.
                - Fiecare valoare sa fie scurta (maxim 1-2 propozitii).
                - Observatiile si concluziile trebuie bazate exclusiv pe: initialArray, finalArray, elementCount, actualComparisons, totalComparisons, totalSwaps, totalSteps, correctlySorted, theoreticalComparisons, comparisonMatch.
                - Foloseste initialArray doar ca date de intrare; foloseste finalArray doar ca rezultat final.
                - Nu spune ca setul era deja sortat si nu presupune distributia datelor.
                - Nu genera recomandari generale despre algoritmi.
                - Nu spune ca un algoritm este potrivit pentru seturi mici.
                - Pentru BITONIC, daca hasFixedComparisonCount=true, spune ca totalComparisons trebuie interpretat ca numar fix pentru aceasta dimensiune, nu ca efect al ordinii initialArray.
                - Pentru ODD_EVEN, mentioneaza early stopping doar daca earlyStoppingUsed=true.
                - Afiseaza separat COMPARATII_TEORETICE si COMPARATII_EXECUTATE, cu valorile numerice.
                - Daca comparisonMatch=true, STATUS trebuie sa fie "Coincid ✓"; daca comparisonMatch=false, STATUS trebuie sa fie "Difera ✗".
                - Nu folosi doar cuvantul "coincid" fara a afisa valorile comparate.

                FORMAT OBLIGATORIU:
                ALGORITMI_ANALIZATI: <algoritmi si numar elemente>
                SWAPURI_EXECUTATE: <swap-uri executate pentru fiecare algoritm>
                PASI_TOTALI: <pasi executati pentru fiecare algoritm>
                SORTARE_CORECTA: <daca finalArray este sortat corect pentru fiecare algoritm>
                COMPARATII_TEORETICE: <valorile theoreticalComparisons pentru fiecare algoritm>
                COMPARATII_EXECUTATE: <valorile actualComparisons pentru fiecare algoritm>
                STATUS: <Coincid ✓ daca comparisonMatch=true, Difera ✗ daca comparisonMatch=false, pentru fiecare algoritm>
                CONCLUZIE_STATISTICA: <ce inseamna verificarea dintre teoretic si executat, strict din metricile rularii curente>
                """.formatted(
                    req.getMode(),
                    req.getSource(),
                    req.getDirection(),
                    req.getValueCount(),
                    inputValues,
                    resultLines
            );
        }

        return """
                Analizeaza strict statisticile rularii curente ale unui singur algoritm de sortare.

                Context rulare:
                - Mod: %s
                - Sursa date: %s
                - Directie: %s
                - Numar valori: %d
                - Vector intrare: [%s]

                Rezultat:
                %s

                Cerinte raspuns:
                - Raspunde STRICT in formatul de mai jos, cate o singura linie per camp.
                - Fara markdown, fara bold, fara comparatii cu alti algoritmi.
                - Observatiile si concluziile trebuie bazate exclusiv pe: initialArray, finalArray, elementCount, actualComparisons, totalComparisons, totalSwaps, totalSteps, correctlySorted, theoreticalComparisons, comparisonMatch.
                - Foloseste initialArray doar ca date de intrare; foloseste finalArray doar ca rezultat final.
                - Nu spune ca setul era deja sortat si nu presupune distributia datelor.
                - Nu genera recomandari generale despre algoritm.
                - Nu spune ca algoritmul este potrivit pentru seturi mici.
                - Pentru BITONIC, daca hasFixedComparisonCount=true, spune ca totalComparisons este fix pentru aceasta dimensiune si nu indica eficientizare pe baza ordinii datelor.
                - Pentru ODD_EVEN, mentioneaza early stopping doar daca earlyStoppingUsed=true.
                - Afiseaza separat COMPARATII_TEORETICE si COMPARATII_EXECUTATE, cu valorile numerice.
                - Daca comparisonMatch=true, STATUS trebuie sa fie "Coincid ✓"; daca comparisonMatch=false, STATUS trebuie sa fie "Difera ✗".
                - Nu folosi doar cuvantul "coincid" fara a afisa valorile comparate.

                FORMAT OBLIGATORIU:
                ALGORITM: <nume algoritm>
                TIMP_MS: <numar>
                PASI_TOTALI: <numar>
                SWAPURI_EXECUTATE: <numar swap-uri necesare>
                SORTARE_CORECTA: <da/nu + referire la correctlySorted/finalArray>
                COMPARATII_TEORETICE: <valoarea theoreticalComparisons>
                COMPARATII_EXECUTATE: <valoarea actualComparisons>
                STATUS: <Coincid ✓ daca comparisonMatch=true, Difera ✗ daca comparisonMatch=false>
                CONCLUZIE_STATISTICA: <ce inseamna verificarea dintre teoretic si executat, strict din statisticile rularii curente>
                """.formatted(
                req.getMode(),
                req.getSource(),
                req.getDirection(),
                req.getValueCount(),
                inputValues,
                resultLines
        );
    }

    private String toResultLine(RunAnalysisResultDto r) {
        return """
                - algorithmName=%s, effectiveAlgorithm=%s, sortDirection=%s, elementCount=%s
                  initialArray=%s
                  finalArray=%s
                  totalSteps=%d, totalComparisons=%d, actualComparisons=%s, totalSwaps=%d, executionTimeMs=%d
                  correctlySorted=%s, isFinalArraySortedCorrectly=%s
                  theoreticalComparisons=%s, comparisonMatch=%s, hasFixedComparisonCount=%s, earlyStoppingUsed=%s
                  comparedWithOtherAlgorithm=%s, otherAlgorithmStats=%s
                """
                .formatted(
                        valueOr(r.getAlgorithmName(), r.getAlgorithm()),
                        r.getEffectiveAlgorithm(),
                        valueOr(r.getSortDirection(), ""),
                        r.getElementCount() == null ? "N/A" : r.getElementCount().toString(),
                        r.getInitialArray(),
                        r.getFinalArray(),
                        r.getTotalSteps(),
                        r.getTotalComparisons(),
                        r.getActualComparisons() == null ? r.getTotalComparisons().toString() : r.getActualComparisons().toString(),
                        r.getTotalSwaps(),
                        r.getExecutionTimeMs(),
                        Boolean.TRUE.equals(r.getCorrectlySorted()),
                        Boolean.TRUE.equals(r.getIsFinalArraySortedCorrectly()),
                        r.getTheoreticalComparisons() == null ? "N/A" : r.getTheoreticalComparisons().toString(),
                        r.getComparisonMatch() == null
                                ? r.getTheoreticalComparisons() != null && r.getTheoreticalComparisons().equals(r.getTotalComparisons())
                                : Boolean.TRUE.equals(r.getComparisonMatch()),
                        Boolean.TRUE.equals(r.getHasFixedComparisonCount()),
                        Boolean.TRUE.equals(r.getEarlyStoppingUsed()),
                        Boolean.TRUE.equals(r.getComparedWithOtherAlgorithm()),
                        r.getOtherAlgorithmStats()
                );
    }

    private String toComparativeResultLine(AlgorithmRunResultDto r) {
        return """
                - algorithmName=%s, executedAs=%s, totalSteps=%d, totalComparisons=%d, totalSwaps=%d, executionTimeMs=%d, correctlySorted=%s, finalArray=%s
                """.formatted(
                r.getAlgorithmName(),
                r.getExecutedAs(),
                r.getTotalSteps(),
                r.getTotalComparisons(),
                r.getTotalSwaps(),
                r.getExecutionTimeMs(),
                Boolean.TRUE.equals(r.getCorrectlySorted()),
                r.getFinalArray()
        );
    }

    private String formatSingleAlgorithmAnalysisResponse(SingleAlgorithmAnalysisRequestDto req, String aiReply) {
        RunAnalysisResultDto result = req.getResult();
        String algorithmName = valueOr(result.getAlgorithmName(), result.getAlgorithm(), "N/A");
        int actualComparisons = result.getActualComparisons() == null ? result.getTotalComparisons() : result.getActualComparisons();
        String theoreticalComparisons = result.getTheoreticalComparisons() == null ? "N/A" : result.getTheoreticalComparisons().toString();
        boolean comparisonMatch = result.getComparisonMatch() == null
                ? result.getTheoreticalComparisons() != null && result.getTheoreticalComparisons().equals(actualComparisons)
                : Boolean.TRUE.equals(result.getComparisonMatch());
        boolean sortedCorrectly = Boolean.TRUE.equals(
                result.getCorrectlySorted() == null ? result.getIsFinalArraySortedCorrectly() : result.getCorrectlySorted()
        );
        String conclusion = extractAnalysisValue(aiReply, "CONCLUZIE_STATISTICA");
        if (conclusion.isBlank()) {
            conclusion = "Rularea curenta pentru " + algorithmName
                    + " a executat " + result.getTotalSteps()
                    + " pasi, " + actualComparisons
                    + " comparatii si " + result.getTotalSwaps()
                    + " swap-uri. Verificarea comparatiilor "
                    + (comparisonMatch ? "coincide cu valoarea teoretica transmisa in DTO." : "difera de valoarea teoretica transmisa in DTO.");
        }

        return """
                ALGORITM: %s
                TIMP_MS: %d
                PASI_TOTALI: %d
                SWAPURI_EXECUTATE: %d
                SORTARE_CORECTA: %s
                COMPARATII_TEORETICE: %s
                COMPARATII_EXECUTATE: %d
                STATUS: %s
                CONCLUZIE_STATISTICA: %s
                """.formatted(
                algorithmName,
                result.getExecutionTimeMs(),
                result.getTotalSteps(),
                result.getTotalSwaps(),
                sortedCorrectly ? "DA" : "NU",
                theoreticalComparisons,
                actualComparisons,
                comparisonMatch ? "Coincid" : "Difera",
                conclusion
        );
    }

    private String formatComparativeAnalysisResponse(ComparativeAnalysisRequestDto req, String aiReply) {
        AlgorithmRunResultDto comparisonWinner = minByComparisons(req.getResults());
        AlgorithmRunResultDto swapWinner = minBySwaps(req.getResults());
        AlgorithmRunResultDto comparisonLoser = maxByComparisons(req.getResults());
        int maxComparisons = maxComparisons(req.getResults());
        int minComparisons = minComparisons(req.getResults());
        int maxSwaps = maxSwaps(req.getResults());
        int minSwaps = minSwaps(req.getResults());
        double comparisonDiffPercent = percentDiff(maxComparisons, minComparisons);
        double swapDiffPercent = percentDiff(maxSwaps, minSwaps);
        String mismatchWarning = req.getResults().stream()
                .filter(result -> !result.getAlgorithmName().equals(result.getExecutedAs()))
                .map(result -> result.getAlgorithmName() + " executat ca " + result.getExecutedAs())
                .collect(Collectors.joining("; "));
        String conclusion = buildDeterministicComparativeConclusion(
                comparisonWinner,
                swapWinner,
                comparisonLoser,
                req.getResults(),
                comparisonDiffPercent,
                swapDiffPercent,
                mismatchWarning
        );

        return """
                ALGORITMI_ANALIZATI: %s
                SORTARE_CORECTA: %s
                COMPARATII: %s
                SWAP_URI: %s
                TIMP_MS: %s
                CASTIGATOR_COMPARATII: %s
                CASTIGATOR_SWAP_URI: %s
                CELE_MAI_MULTE_COMPARATII: %s = %d
                DIFERENTA_COMPARATII: (%d - %d) / %d * 100 = %.2f%%
                DIFERENTA_SWAP_URI: (%d - %d) / %d * 100 = %.2f%%
                CONCLUZIE_COMPARATIVA: %s
                """.formatted(
                req.getResults().stream().map(AlgorithmRunResultDto::getAlgorithmName).collect(Collectors.joining(", ")),
                comparativeMetricLine(req.getResults(), result -> Boolean.TRUE.equals(result.getCorrectlySorted()) ? "DA" : "NU"),
                comparativeMetricLine(req.getResults(), result -> result.getTotalComparisons().toString()),
                comparativeMetricLine(req.getResults(), result -> result.getTotalSwaps().toString()),
                comparativeMetricLine(req.getResults(), result -> result.getExecutionTimeMs().toString()),
                comparisonWinner == null ? "N/A" : comparisonWinner.getAlgorithmName(),
                swapWinner == null ? "N/A" : swapWinner.getAlgorithmName(),
                comparisonLoser == null ? "N/A" : comparisonLoser.getAlgorithmName(),
                maxComparisons,
                maxComparisons,
                minComparisons,
                maxComparisons,
                comparisonDiffPercent,
                maxSwaps,
                minSwaps,
                maxSwaps,
                swapDiffPercent,
                conclusion
        );
    }

    private String buildDeterministicComparativeConclusion(
            AlgorithmRunResultDto comparisonWinner,
            AlgorithmRunResultDto swapWinner,
            AlgorithmRunResultDto comparisonLoser,
            List<AlgorithmRunResultDto> results,
            double comparisonDiffPercent,
            double swapDiffPercent,
            String mismatchWarning
    ) {
        String base = "Comparatia foloseste toate rezultatele din lista: "
                + "castigator la comparatii este " + (comparisonWinner == null ? "N/A" : comparisonWinner.getAlgorithmName())
                + ", iar castigator la swap-uri este " + (swapWinner == null ? "N/A" : swapWinner.getAlgorithmName())
                + ". Algoritmul cu cele mai multe comparatii este " + (comparisonLoser == null ? "N/A" : comparisonLoser.getAlgorithmName())
                + " cu " + (comparisonLoser == null ? 0 : comparisonLoser.getTotalComparisons()) + " comparatii."
                + " Diferenta este de %.2f%% la comparatii si %.2f%% la swap-uri.".formatted(comparisonDiffPercent, swapDiffPercent);
        String identicalStats = identicalStatisticsGroups(results);
        if (!identicalStats.isBlank()) {
            base += " Statistici identice detectate: " + identicalStats + ".";
        }
        if (comparisonLoser != null && "PAIRWISE_SORTING_NETWORK".equals(comparisonLoser.getAlgorithmName())) {
            base += " PAIRWISE_SORTING_NETWORK este cel mai slab la criteriul comparatii pentru aceasta rulare, deoarece are valoarea maxima.";
        }
        if (mismatchWarning != null && !mismatchWarning.isBlank()) {
            return base + " Atentie: exista mapping invalid (" + mismatchWarning + "), deci comparatia nu trebuie considerata finala.";
        }
        return base;
    }

    private String extractAnalysisValue(String text, String expectedKey) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalizedExpected = expectedKey.replace("_", "");
        for (String line : text.split("\\R")) {
            int idx = line.indexOf(':');
            if (idx < 0) {
                continue;
            }
            String key = line.substring(0, idx)
                    .replace("*", "")
                    .replace("_", "")
                    .replaceAll("\\s+", "")
                    .toUpperCase();
            if (normalizedExpected.equals(key)) {
                return line.substring(idx + 1)
                        .replace("*", "")
                        .trim();
            }
        }
        return "";
    }

    private AlgorithmRunResultDto minByComparisons(List<AlgorithmRunResultDto> results) {
        return results.stream()
                .min((left, right) -> Integer.compare(left.getTotalComparisons(), right.getTotalComparisons()))
                .orElse(null);
    }

    private AlgorithmRunResultDto minBySwaps(List<AlgorithmRunResultDto> results) {
        return results.stream()
                .min((left, right) -> Integer.compare(left.getTotalSwaps(), right.getTotalSwaps()))
                .orElse(null);
    }

    private AlgorithmRunResultDto maxByComparisons(List<AlgorithmRunResultDto> results) {
        return results.stream()
                .max((left, right) -> Integer.compare(left.getTotalComparisons(), right.getTotalComparisons()))
                .orElse(null);
    }

    private String identicalStatisticsGroups(List<AlgorithmRunResultDto> results) {
        return results.stream()
                .collect(Collectors.groupingBy(
                        result -> result.getTotalComparisons() + "/" + result.getTotalSwaps() + "/" + result.getTotalSteps(),
                        Collectors.mapping(AlgorithmRunResultDto::getAlgorithmName, Collectors.toList())
                ))
                .values()
                .stream()
                .filter(names -> names.size() > 1)
                .map(names -> String.join(" si ", names))
                .collect(Collectors.joining("; "));
    }

    private int minComparisons(List<AlgorithmRunResultDto> results) {
        return results.stream().mapToInt(AlgorithmRunResultDto::getTotalComparisons).min().orElse(0);
    }

    private int maxComparisons(List<AlgorithmRunResultDto> results) {
        return results.stream().mapToInt(AlgorithmRunResultDto::getTotalComparisons).max().orElse(0);
    }

    private int minSwaps(List<AlgorithmRunResultDto> results) {
        return results.stream().mapToInt(AlgorithmRunResultDto::getTotalSwaps).min().orElse(0);
    }

    private int maxSwaps(List<AlgorithmRunResultDto> results) {
        return results.stream().mapToInt(AlgorithmRunResultDto::getTotalSwaps).max().orElse(0);
    }

    private double percentDiff(int max, int min) {
        if (max <= 0) {
            return 0.0;
        }
        return ((double) (max - min) / max) * 100.0;
    }

    private String comparativeMetricLine(List<AlgorithmRunResultDto> results, Function<AlgorithmRunResultDto, String> valueExtractor) {
        return results.stream()
                .map(result -> result.getAlgorithmName() + " = " + valueExtractor.apply(result))
                .collect(Collectors.joining(", "));
    }

    private String buildClassroomAnalysisPrompt(ClassroomAnalysisRequest req) {
        String studentLines = safeList(req.getStudentStats()).stream()
                .map(student -> "- studentId=%s, completed=%d, inProgress=%d, notStarted=%d, totalRuns=%d, comparisons=%d, swaps=%d, steps=%d, averageQuizScore=%.2f"
                        .formatted(
                                student.getStudentId(),
                                student.getCompletedAssignments(),
                                student.getInProgressAssignments(),
                                student.getNotStartedAssignments(),
                                student.getTotalRuns(),
                                student.getTotalComparisons(),
                                student.getTotalSwaps(),
                                student.getTotalSteps(),
                                student.getAverageQuizScore()
                        ))
                .collect(Collectors.joining("\n"));
        String assignmentLines = safeList(req.getAssignmentStats()).stream()
                .map(assignment -> "- assignmentId=%s, title=%s, algorithm=%s, direction=%s, status=%s, completed=%d, inProgress=%d, notStarted=%d, totalRuns=%d, averageQuizScore=%.2f"
                        .formatted(
                                assignment.getAssignmentId(),
                                assignment.getTitle(),
                                assignment.getAlgorithm(),
                                assignment.getDirection(),
                                assignment.getStatus(),
                                assignment.getCompletedCount(),
                                assignment.getInProgressCount(),
                                assignment.getNotStartedCount(),
                                assignment.getTotalRuns(),
                                assignment.getAverageQuizScore()
                        ))
                .collect(Collectors.joining("\n"));
        return """
                Analizeaza o clasa din platforma Algorithm Visualizer pentru profesor.
                Foloseste exclusiv datele din DTO. Nu inventa informatii si nu folosi sabloane cu chei precum CONCEPTE_INTELESE, CONCEPTE_DIFICILE sau GRESELI_FRECVENTE.
                Fiecare afirmatie trebuie justificata prin valori concrete din DTO.

                DTO agregat clasa:
                - ID: %d
                - Nume: %s
                - numarElevi: %d
                - numarTeme: %d
                - temeFinalizate: %d
                - temeNeincepute: %d
                - temeInProgres: %d
                - rataFinalizare: %.2f%%
                - scorMediu: %.2f
                - rulariMediiElevTema: %.2f
                - numarTotalRulari: %d
                - algoritmCelMaiFolosit: %s
                - algoritmiUtilizati: %s
                - distributieRezultate: %s
                - eleviCareNecesitaAtentie: %s

                Statistici pe fiecare elev:
                %s

                Statistici pe fiecare tema:
                %s

                Reguli obligatorii:
                - Raspunde in romana.
                - Nu folosi markdown bold si nu folosi chei tehnice cu underscore.
                - Structureaza raspunsul exact in sectiunile de mai jos.
                - Daca exista un singur elev, spune explicit acest lucru.
                - Daca exista teme neincepute, identifica temele dupa titlu.
                - Daca exista elevi fara activitate, identifica elevii dupa studentId.
                - Daca scorul mediu este ridicat, mentioneaza valoarea scorului mediu.
                - Daca rata de finalizare este scazuta, explica impactul asupra interpretarii clasei.
                - Nu repeta doar datele; interpreteaza util pentru profesor, dar fiecare observatie trebuie sa fie sustinuta de campurile DTO.

                Format obligatoriu:
                Situatia clasei
                <analiza bazata pe numarElevi, numarTeme si statusuri>

                Performanta generala
                <analiza bazata pe rataFinalizare, scorMediu si numarTotalRulari>

                Observatii importante
                <observatii despre teme, algoritmi, activitate si statusuri>

                Elevii care necesita atentie
                <studentId si motivul sustinut de studentStats>

                Recomandari pentru profesor
                <actiuni concrete legate de datele clasei>

                Concluzie
                <concluzie scurta bazata pe DTO>
                """.formatted(
                req.getClassId(),
                req.getClassName(),
                req.getStudentCount(),
                req.getAssignmentCount(),
                req.getCompletedAssignments(),
                req.getNotStartedAssignments(),
                req.getInProgressAssignments(),
                req.getCompletionRate(),
                req.getAverageQuizScore(),
                req.getAverageRunCount(),
                req.getTotalRuns(),
                req.getMostUsedAlgorithm(),
                req.getAlgorithmsUsed(),
                req.getResultDistribution(),
                req.getStudentsNeedingAttention(),
                studentLines.isBlank() ? "N/A" : studentLines,
                assignmentLines.isBlank() ? "N/A" : assignmentLines
        );
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
