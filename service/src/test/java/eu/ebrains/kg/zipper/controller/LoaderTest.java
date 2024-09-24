package eu.ebrains.kg.zipper.controller;

import eu.ebrains.kg.zipper.models.Container;
import eu.ebrains.kg.zipper.models.FileReference;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoaderTest {
    private final Loader loader = new Loader() {
        @Override
        public List<FileReference> getUrls(Container container) {
            return null;
        }

        @Override
        protected Flux<DataBuffer> getDataBufferFlux(URI encodedUrl, Optional<String> token) throws URISyntaxException, MalformedURLException {
            return null;
        }
    };

    @Test
    void getEncodedURIWithHash() {
        URI encodedURI = loader.getEncodedURI("https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d002323_NeuromodNeocortex-EPhys_pub/Acetylcholine modulation in S1 cortex layer 6/L6A paired-recodings OG data(ACh)/E-E/BC 19112015 EF/EF#V8.pgf");
        assertEquals("https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d002323_NeuromodNeocortex-EPhys_pub/Acetylcholine%20modulation%20in%20S1%20cortex%20layer%206/L6A%20paired-recodings%20OG%20data(ACh)/E-E/BC%2019112015%20EF/EF%23V8.pgf", encodedURI.toString());
    }

    @Test
    void getEncodedURI() {
        URI encodedURI = loader.getEncodedURI("https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d002323_NeuromodNeocortex-EPhys_pub/Acetylcholine modulation in S1 cortex layer 6/L6A paired-recodings OG data(ACh)/E-E/BC 19112015 EF/EF V8.pgf");
        assertEquals("https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d002323_NeuromodNeocortex-EPhys_pub/Acetylcholine%20modulation%20in%20S1%20cortex%20layer%206/L6A%20paired-recodings%20OG%20data(ACh)/E-E/BC%2019112015%20EF/EF%20V8.pgf", encodedURI.toString());
    }
}