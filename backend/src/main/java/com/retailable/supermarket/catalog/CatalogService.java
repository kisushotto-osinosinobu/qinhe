package com.retailable.supermarket.catalog;

import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.common.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.retailable.supermarket.catalog.CatalogDtos.*;

@Service
public class CatalogService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final AuditService audit;
    private final Path uploadDir;

    public CatalogService(CategoryMapper categoryMapper, ProductMapper productMapper, AuditService audit,
                          @Value("${app.upload-dir}") String uploadDir) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.audit = audit;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public PageResult<Map<String,Object>> categories(String keyword, int page, int size) {
        int p = Math.max(page,1), s = Math.min(Math.max(size,1),100);
        return PageResult.of(categoryMapper.page(keyword,(p-1)*s,s),categoryMapper.count(keyword),p,s);
    }

    public List<Map<String,Object>> publicCategories() { return categoryMapper.enabled(); }

    @Transactional
    public Map<String,Object> createCategory(CategoryRequest r) {
        CategoryMapper.CategoryRow row = categoryRow(null,r);
        categoryMapper.insert(row);
        audit.record("CATEGORY_CREATE","CATEGORY",row.id,row.name);
        return categoryMapper.findById(row.id);
    }

    @Transactional
    public Map<String,Object> updateCategory(Long id, CategoryRequest r) {
        requireCategory(id);
        CategoryMapper.CategoryRow row = categoryRow(id,r);
        categoryMapper.update(row);
        audit.record("CATEGORY_UPDATE","CATEGORY",id,row.name);
        return categoryMapper.findById(id);
    }

    @Transactional
    public void deleteCategory(Long id) {
        requireCategory(id);
        if (categoryMapper.productCount(id)>0) throw BusinessException.conflict("分类已关联商品，不能删除；可改为停用");
        categoryMapper.delete(id);
        audit.record("CATEGORY_DELETE","CATEGORY",id,"删除空分类");
    }

    public PageResult<Map<String,Object>> products(String keyword, Long categoryId, boolean publicOnly, int page, int size) {
        int p=Math.max(page,1), s=Math.min(Math.max(size,1),100);
        return PageResult.of(productMapper.page(keyword,categoryId,publicOnly,(p-1)*s,s),productMapper.count(keyword,categoryId,publicOnly),p,s);
    }

    public Map<String,Object> product(Long id, boolean publicOnly) {
        Map<String,Object> result=productMapper.findById(id);
        if (result==null || (publicOnly && !"ON_SALE".equals(result.get("status")))) throw BusinessException.notFound("商品不存在或已下架");
        return result;
    }

    public Map<String,Object> barcode(String barcode) {
        Map<String,Object> result=productMapper.findByBarcode(barcode);
        if (result==null || !"ON_SALE".equals(result.get("status"))) throw BusinessException.notFound("未找到可售商品");
        return result;
    }

    @Transactional
    public Map<String,Object> createProduct(ProductRequest r) {
        requireCategory(r.categoryId());
        ProductMapper.ProductRow row=productRow(null,r);
        productMapper.insert(row);
        productMapper.initInventory(row.id);
        audit.record("PRODUCT_CREATE","PRODUCT",row.id,row.name);
        return productMapper.findById(row.id);
    }

    @Transactional
    public Map<String,Object> updateProduct(Long id, ProductRequest r) {
        requireProduct(id); requireCategory(r.categoryId());
        ProductMapper.ProductRow row=productRow(id,r);
        productMapper.update(row);
        audit.record("PRODUCT_UPDATE","PRODUCT",id,row.name);
        return productMapper.findById(id);
    }

    public Map<String,String> upload(MultipartFile file) {
        if (file==null || file.isEmpty()) throw BusinessException.badRequest("请选择图片");
        String type=Optional.ofNullable(file.getContentType()).orElse("");
        Map<String,String> extensions=Map.of("image/jpeg","jpg","image/png","png","image/webp","webp","image/gif","gif");
        String ext=extensions.get(type.toLowerCase(Locale.ROOT));
        if (ext==null) throw BusinessException.badRequest("仅支持 JPG、PNG、WebP 或 GIF 图片");
        try {
            Files.createDirectories(uploadDir);
            String name=UUID.randomUUID()+"."+ext;
            Path target=uploadDir.resolve(name).normalize();
            if (!target.startsWith(uploadDir)) throw BusinessException.badRequest("文件名无效");
            Files.copy(file.getInputStream(),target, StandardCopyOption.REPLACE_EXISTING);
            audit.record("IMAGE_UPLOAD","PRODUCT",null,name);
            return Map.of("url","/uploads/"+name);
        } catch (IOException ex) {
            throw new BusinessException(5001, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,"图片保存失败");
        }
    }

    private void requireCategory(Long id) { if(categoryMapper.findById(id)==null) throw BusinessException.notFound("分类不存在"); }
    private void requireProduct(Long id) { if(productMapper.findById(id)==null) throw BusinessException.notFound("商品不存在"); }
    private CategoryMapper.CategoryRow categoryRow(Long id, CategoryRequest r){ var x=new CategoryMapper.CategoryRow(); x.id=id;x.name=r.name();x.code=r.code();x.sortOrder=r.sortOrder();x.status=r.status();return x; }
    private ProductMapper.ProductRow productRow(Long id, ProductRequest r){ var x=new ProductMapper.ProductRow();x.id=id;x.code=r.code();x.barcode=r.barcode();x.name=r.name();x.categoryId=r.categoryId();x.specification=r.specification();x.unit=r.unit();x.purchasePrice=r.purchasePrice();x.salePrice=r.salePrice();x.imageUrl=r.imageUrl();x.status=r.status();x.lowStockThreshold=r.lowStockThreshold();return x; }
}

