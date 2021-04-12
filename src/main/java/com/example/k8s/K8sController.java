package com.example.k8s;

import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequiredArgsConstructor
public class K8sController {

    private final KubernetesClient client;

    @GetMapping("/api/version")
    public ServiceList apiVersion() {
        return client
                .services()
                .list();
    }

    @GetMapping("/api/deployment")
    public Object someApi() {


        AtomicInteger i = new AtomicInteger();
        AtomicBoolean bool = new AtomicBoolean(true);

        return Mono
                .fromCallable(i::incrementAndGet)
                .doOnNext(v -> {
                    log.warn("{}: {}", v, Thread
                            .currentThread()
                            .getName());
                    if (v < 4) {
                        throw new RuntimeException("test");
                    } else {
                        bool.set(false);
                    }
                })
                .retryWhen(Retry
                        .fixedDelay(100, Duration.ofSeconds(1L))
                        .filter(e -> bool.get()))
                .subscribe(k -> {
                    log.error("{}: {}", k, Thread
                            .currentThread()
                            .getName());
                });

    }

}
