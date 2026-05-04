/** 世界杯论坛 - 监控聚合（Micrometer 与 Actuator 同源指标，供 ECharts 使用） */
package com.worldcup.forum.controller;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将 JVM / 进程类指标聚合成单份 JSON，便于前端 ECharts 轮询绘图。
 * 底层数据与 {@code /actuator/metrics} 一致，均来自 {@link MeterRegistry}。
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MeterRegistry meterRegistry;

    public MonitorController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/snapshot")
    public Map<String, Object> snapshot() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toEpochMilli());
        body.put("heapUsedMb", round2(sumHeapBytes("jvm.memory.used") / 1024.0 / 1024.0));
        body.put("heapMaxMb", round2(sumHeapBytes("jvm.memory.max") / 1024.0 / 1024.0));
        body.put("nonHeapUsedMb", round2(sumAreaBytes("jvm.memory.used", "nonheap") / 1024.0 / 1024.0));
        body.put("threadsLive", (int) gaugeValue("jvm.threads.live"));
        body.put("threadsPeak", (int) gaugeValue("jvm.threads.peak"));
        body.put("cpuProcess", round4(gaugeValue("process.cpu.usage")));
        body.put("cpuSystem", round4(gaugeValue("system.cpu.usage")));
        body.put("uptimeSeconds", round2(gaugeValue("process.uptime")));
        return body;
    }

    private double sumHeapBytes(String metric) {
        return sumAreaBytes(metric, "heap");
    }

    private double sumAreaBytes(String metric, String area) {
        return meterRegistry.find(metric).tag("area", area).gauges().stream()
                .mapToDouble(Gauge::value)
                .filter(v -> !Double.isNaN(v))
                .sum();
    }

    private double gaugeValue(String name) {
        Gauge g = meterRegistry.find(name).gauge();
        if (g == null) {
            return Double.NaN;
        }
        double v = g.value();
        return Double.isNaN(v) ? 0 : v;
    }

    private static double round2(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return 0;
        }
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round4(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return 0;
        }
        return Math.round(v * 10000.0) / 10000.0;
    }
}
