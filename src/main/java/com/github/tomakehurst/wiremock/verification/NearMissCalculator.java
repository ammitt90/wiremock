package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServedStub;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.Math.min;

public class NearMissCalculator {

    public static final int NEAR_MISS_COUNT = 3;

    public static final Comparator<NearMiss> NEAR_MISS_ASCENDING_COMPARATOR = new Comparator<NearMiss>() {
        public int compare(NearMiss o1, NearMiss o2) {
            return o1.compareTo(o2);
        }
    };

    private final StubMappings stubMappings;
    private final RequestJournal requestJournal;

    public NearMissCalculator(StubMappings stubMappings, RequestJournal requestJournal) {
        this.stubMappings = stubMappings;
        this.requestJournal = requestJournal;
    }

    public List<NearMiss> findNearestTo(final LoggedRequest request) {
        List<StubMapping> allMappings = stubMappings.getAll();

        return sortAndTruncate(from(allMappings).transform(new Function<StubMapping, NearMiss>() {
            public NearMiss apply(StubMapping stubMapping) {
                MatchResult matchResult = stubMapping.getRequest().match(request);
                return new NearMiss(request, stubMapping, matchResult);
            }
        }), allMappings.size());
    }

    public List<NearMiss> findNearestTo(final RequestPattern requestPattern) {
        List<ServedStub> servedStubs = requestJournal.getAllServedStubs();
        return sortAndTruncate(from(servedStubs).transform(new Function<ServedStub, NearMiss>() {
            public NearMiss apply(ServedStub servedStub) {
                MatchResult matchResult = requestPattern.match(servedStub.getRequest());
                return new NearMiss(servedStub.getRequest(), requestPattern, matchResult);
            }
        }), servedStubs.size());
    }

    private static List<NearMiss> sortAndTruncate(FluentIterable<NearMiss> nearMisses, int originalSize) {
        return nearMisses
            .toSortedList(NEAR_MISS_ASCENDING_COMPARATOR)
            .subList(0, min(NEAR_MISS_COUNT, originalSize));
    }


}
