package com.google.cloud.firestore.demo.storage;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.demo.model.Discount;
import com.google.cloud.firestore.demo.model.Product;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class FirestoreService {
    private final String productsCollectionName = "products";
    private final ExecutorService executorService;
    private Firestore db;

    public FirestoreService() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @PostConstruct
    public void initialize() throws IOException {
        InputStream serviceAccount = this.getClass().getClassLoader().getResourceAsStream("ambient-stone-255607-14320cd3c2dc.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);
        db = FirestoreClient.getFirestore();
    }

    public String save(Product product) throws ExecutionException, InterruptedException {
        DocumentReference productDocRef = db.collection(productsCollectionName).document();
        String id = productDocRef.getId();
        product.setId(id);
        productDocRef.set(product).get();
        return id;
    }

    public String update(String id, Product product) throws ExecutionException, InterruptedException {
        String productId = StringUtils.isEmpty(product.getId()) ? id : product.getId();
        product.setId(productId);
        db.collection(productsCollectionName)
                .document(productId)
                .set(product)
                .get();
        return product.getId();
    }

    public Product getById(String id) throws ExecutionException, InterruptedException {
        return db.collection(productsCollectionName)
                .document(id)
                .get()
                .get()
                .toObject(Product.class);
    }

    public String updateProductDiscount(String id, Discount discount) throws ExecutionException, InterruptedException {
        /*
         *  productDocRef.get();
         * FieldValue.increment()
         * */
        db.collection(productsCollectionName)
                .document(id)
                .update(FieldPath.of("price", "discount"), PojoToClassMapper.map(discount))
                .get();
        return id;
    }

    public String parallelAvailableQuantityUpdate(String id) throws ExecutionException, InterruptedException {
        Product product = db.collection(productsCollectionName)
                .document(id)
                .get()
                .get()
                .toObject(Product.class);

        Future<?> submit = executorService.submit(() -> {
            updateAvailableQuantityTo200(id);
            return "";
        });
        submit.get();

        if (product.getAvailableQuantity() < 100) {
            product.setAvailableQuantity(100);
        }
        db.collection(productsCollectionName)
                .document(id)
                .set(product)
                .get();

        return id;
    }

    public String parallelTransactionalAvailableQuantityUpdate(String id) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = db.collection(productsCollectionName)
                .document(id);

        ApiFuture<String> transactionFuture = db.runTransaction(transaction -> {
            Product product = transaction.get(documentReference)
                    .get()
                    .toObject(Product.class);

            Future<?> updateAqTo200 = executorService.submit(() -> {
                Thread.sleep(1000);
                updateAvailableQuantityTo200(id);
                return "";
            });
            updateAqTo200.get();

            if (product.getAvailableQuantity() < 200) {
                product.setAvailableQuantity(100);
            }
            transaction.set(documentReference, product);
            return id;
        });
        return transactionFuture.get();
    }

    private void updateAvailableQuantityTo200(String id) {
        try {
            Product product1 = db.collection(productsCollectionName)
                    .document(id)
                    .get()
                    .get()
                    .toObject(Product.class);
            if (product1.getAvailableQuantity() >= 200) {
                return;
            }
            product1.setAvailableQuantity(200);
            db.collection(productsCollectionName)
                    .document(id)
                    .set(product1)
                    .get();
            System.out.println(String.format("Udpated %s to 200", id));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}