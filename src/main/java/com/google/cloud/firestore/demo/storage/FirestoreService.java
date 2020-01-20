package com.google.cloud.firestore.demo.storage;

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

@Service
public class FirestoreService {
    private final String productsCollectionName = "products";
    private Firestore db;

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
}