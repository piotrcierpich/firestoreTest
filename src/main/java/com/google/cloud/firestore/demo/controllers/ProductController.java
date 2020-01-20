package com.google.cloud.firestore.demo.controllers;

import com.google.cloud.firestore.demo.model.Discount;
import com.google.cloud.firestore.demo.model.Product;
import com.google.cloud.firestore.demo.storage.FirestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
public class ProductController {
    FirestoreService firestoreService;

    @Autowired
    public ProductController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping(path = "collection/{id}")
    public ResponseEntity get(@PathVariable String id) throws ExecutionException, InterruptedException {
        Product product = firestoreService.getById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping(path = "collection/{id}")
    public ResponseEntity put(@PathVariable String id, @RequestBody Product product) throws ExecutionException, InterruptedException {
        id = firestoreService.update(id, product);
        return ResponseEntity.ok(id);
    }


    @PutMapping(path = "collection/{id}/discount")
    public ResponseEntity updateDiscount(@PathVariable String id, @RequestBody Discount discount) throws ExecutionException, InterruptedException {
        id = firestoreService.updateProductDiscount(id, discount);
        return ResponseEntity.ok(id);
    }

    @PostMapping(path = "collection")
    public ResponseEntity post(@RequestBody Product product) throws ExecutionException, InterruptedException {
        String result = firestoreService.save(product);
        return ResponseEntity.ok(result);
    }
}
