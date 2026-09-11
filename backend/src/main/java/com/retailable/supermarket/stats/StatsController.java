package com.retailable.supermarket.stats;

import com.retailable.supermarket.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/stats") @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER','CASHIER')")
public class StatsController {
    private final StatsMapper mapper;public StatsController(StatsMapper mapper){this.mapper=mapper;}
    @GetMapping("/dashboard") public ApiResponse<Map<String,Object>> dashboard(){var data=new LinkedHashMap<>(mapper.today());data.put("productCount",mapper.productCount());data.put("lowStockCount",mapper.lowStockCount());data.put("salesDefinition","已付款订单实收金额减退货金额；待付款和已取消订单不计入");return ApiResponse.ok(data);}
    @GetMapping("/sales-trend") public ApiResponse<List<Map<String,Object>>> trend(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate to){range(from,to);return ApiResponse.ok(mapper.trend(from,to));}
    @GetMapping("/hot-products") public ApiResponse<List<Map<String,Object>>> hot(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate to,@RequestParam(defaultValue="10")int limit){range(from,to);return ApiResponse.ok(mapper.hot(from,to,Math.min(100,Math.max(1,limit))));}
    @GetMapping(value="/orders.csv",produces="text/csv;charset=UTF-8") @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public void export(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate to,HttpServletResponse response)throws IOException{range(from,to);response.setHeader("Content-Disposition","attachment; filename=sales-"+from+"-"+to+".csv");var w=response.getOutputStream();w.write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF});w.write("订单号,渠道,状态,订单金额,实收金额,退款金额,净销售额,付款方式,付款时间,创建时间,会员,收银员\r\n".getBytes(StandardCharsets.UTF_8));for(var row:mapper.exportOrders(from,to)){String line=String.join(",",List.of(csv(row.get("orderNo")),csv(row.get("channel")),csv(row.get("status")),csv(row.get("totalAmount")),csv(row.get("paidAmount")),csv(row.get("refundAmount")),csv(row.get("netAmount")),csv(row.get("paymentMethod")),csv(row.get("paidAt")),csv(row.get("createdAt")),csv(row.get("memberUsername")),csv(row.get("cashierUsername"))))+"\r\n";w.write(line.getBytes(StandardCharsets.UTF_8));}}
    private void range(LocalDate from,LocalDate to){if(from==null||to==null||from.isAfter(to)||from.plusYears(1).isBefore(to))throw com.retailable.supermarket.common.BusinessException.badRequest("日期范围无效，最长一年");}
    private String csv(Object value){String s=value==null?"":String.valueOf(value);if(!s.isEmpty()&&"=+-@".indexOf(s.charAt(0))>=0)s="'"+s;return "\""+s.replace("\"","\"\"")+"\"";}
}
