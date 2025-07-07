package com.snow.al.dd.application.mock.controller;

import com.snow.al.dd.core.batch.exec.DdBatchExecutor;
import com.snow.al.dd.core.batch.pack.BatchDdRequestConsumer;
import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.single.exec.DdSingleExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestMongoController {

    private final BatchDdRequestConsumer batchDdRequestConsumer;
    private final DdBatchExecutor ddBatchExecutor;
    private final DdSingleExecutor ddSingleExecutor;


    @GetMapping("/testPack")
    public void testPack() {
        for (int i = 0; i < 11; i++) {
            new Thread(() -> {
                List<DdRequest> a = new ArrayList<>();
                a.add(new DdRequest("abc" + System.currentTimeMillis()));
                batchDdRequestConsumer.consume(a);
            }).start();
        }
    }

    @GetMapping("/testExec/{batchId}")
    public void testExec(@PathVariable("batchId") String batchId) {
        ddBatchExecutor.executeNormalStatus(batchId);
    }

    @GetMapping("/testExec/single/{id}")
    public void testSingleExec(@PathVariable("id") String id) {
        ddSingleExecutor.executeNormalStatus(id);
    }


}