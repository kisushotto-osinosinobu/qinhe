package com.retailable.supermarket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CatalogSupplierIntegrationIT {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void catalogCrudConstraintsAndValidationAreEnforced() throws Exception {
        String token = login();
        String suffix = Long.toString(System.nanoTime());
        String category = "{\"name\":\"测试分类" + suffix + "\",\"code\":\"TEST_" + suffix + "\",\"sortOrder\":90,\"status\":\"ENABLED\"}";
        long categoryId = id(postJson("/api/catalog/categories", token, category, 200));

        String product = "{\"code\":\"P-" + suffix + "\",\"barcode\":\"88" + suffix + "\",\"name\":\"测试商品\",\"categoryId\":" + categoryId + ",\"specification\":\"1件\",\"unit\":\"件\",\"purchasePrice\":\"3.20\",\"salePrice\":\"5.60\",\"status\":\"ON_SALE\",\"lowStockThreshold\":2}";
        id(postJson("/api/catalog/products", token, product, 200));
        postJson("/api/catalog/products", token, product, 409);

        String invalid = product.replace("\"purchasePrice\":\"3.20\"", "\"purchasePrice\":\"-1.00\"").replace("P-" + suffix, "I-" + suffix).replace("88" + suffix, "77" + suffix);
        postJson("/api/catalog/products", token, invalid, 400);
        mvc.perform(delete("/api/catalog/categories/" + categoryId).header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/catalog/products").header("Authorization", bearer(token)).param("keyword", "P-" + suffix))
                .andExpect(status().isOk());
        mvc.perform(get("/api/catalog/public/products").param("categoryId", "undefined"))
                .andExpect(status().isBadRequest());

        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
        MockMultipartFile image = new MockMultipartFile("file", "pixel.png", "image/png", png);
        String upload = mvc.perform(multipart("/api/catalog/products/images").file(image).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String imageUrl = json.readTree(upload).path("data").path("url").asText();
        mvc.perform(get(imageUrl)).andExpect(status().isOk());
        MockMultipartFile invalidFile = new MockMultipartFile("file", "note.txt", "text/plain", "not an image".getBytes());
        mvc.perform(multipart("/api/catalog/products/images").file(invalidFile).header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void supplierCrudAndPurchaseReferenceRestrictionAreEnforced() throws Exception {
        String token = login();
        String suffix = Long.toString(System.nanoTime());
        String supplier = supplierJson("SUP-" + suffix, "临时供应商" + suffix, "ENABLED", "valid@example.test");
        long disposableId = id(postJson("/api/suppliers", token, supplier, 200));
        String updated = supplierJson("SUP-" + suffix, "临时供应商已更新" + suffix, "DISABLED", "new@example.test");
        mvc.perform(put("/api/suppliers/" + disposableId).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(updated))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/suppliers/" + disposableId).header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        postJson("/api/suppliers", token, supplierJson("BAD-" + suffix, "错误邮箱", "ENABLED", "not-an-email"), 400);
        long referencedId = id(postJson("/api/suppliers", token, supplierJson("REF-" + suffix, "采购供应商" + suffix, "ENABLED", "ref@example.test"), 200));
        String purchase = "{\"supplierId\":" + referencedId + ",\"remark\":\"约束测试\",\"items\":[{\"productId\":1,\"quantity\":1,\"unitPrice\":\"1.20\"}]}";
        postJson("/api/purchases", token, purchase, 200);
        mvc.perform(delete("/api/suppliers/" + referencedId).header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
    }

    private String supplierJson(String code, String name, String state, String email) {
        return "{\"code\":\"" + code + "\",\"name\":\"" + name + "\",\"contactName\":\"测试联系人\",\"phone\":\"13800000000\",\"email\":\"" + email + "\",\"address\":\"测试地址\",\"status\":\"" + state + "\",\"remark\":\"集成测试\"}";
    }

    private String postJson(String path, String token, String body, int expectedStatus) throws Exception {
        return mvc.perform(post(path).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
    }

    private long id(String response) throws Exception {
        return json.readTree(response).path("data").path("id").asLong();
    }

    private String login() throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Passw0rd!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode root = json.readTree(response);
        return root.path("data").path("token").asText();
    }

    private String bearer(String token) { return "Bearer " + token; }
}
