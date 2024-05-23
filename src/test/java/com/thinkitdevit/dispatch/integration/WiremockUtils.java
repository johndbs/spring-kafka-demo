package com.thinkitdevit.dispatch.integration;

import com.github.tomakehurst.wiremock.client.WireMock;

public class WiremockUtils {

    public static void reset() {
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.resetAllScenarios();
        WireMock.resetToDefault();
    }


    public static void stubWiremock(String url, int httpStatusResponse, String body){
        stubWiremock(url, httpStatusResponse, body, null, null, null);
    }

    public static void stubWiremock(String url,
                                    int httpStatusResponse,
                                    String body,
                                    String scenarioName,
                                    String initialState,
                                    String nextState){
        if(scenarioName != null) {
            WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(url))
                    .inScenario(scenarioName)
                    .whenScenarioStateIs(initialState)
                    .willReturn(WireMock.aResponse()
                            .withStatus(httpStatusResponse)
                            .withHeader("Content-Type", "text/plain")
                            .withBody(body))
                    .willSetStateTo(nextState));
        } else {
            WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(url))
                    .willReturn(WireMock.aResponse()
                            .withStatus(httpStatusResponse)
                            .withHeader("Content-Type", "text/plain")
                            .withBody(body)));
        }


    }

}
