# ding-server 金蝶集成模块

基于 RuoYi-Vue-Plus 框架的金蝶云星空（K3Cloud）集成模块，提供与金蝶ERP系统的标准接口集成能力。

## 模块结构

```
ding-server/
├── src/main/java/com/ruoyi/
│   ├── middle/
│   │   ├── common/
│   │   │   └── SaveParamNotVerify.java          # 保存参数包装类
│   │   └── util/
│   │       ├── DateUtil.java                    # 日期工具类
│   │       └── KingdeeK3CloudUtil.java          # 金蝶API工具类
│   └── kingdee/
│       ├── constant/
│       │   ├── DateConverterUtil.java            # 日期转换工具
│       │   └── FieldTypeConstant.java           # 字段类型常量
│       ├── controller/
│       │   └── K3CloudProxyController.java      # 金蝶接口控制器
│       ├── dto/
│       │   └── KingdeeLoginRequest.java         # 登录请求DTO
│       ├── service/
│       │   ├── K3CloudProxyService.java         # 服务接口
│       │   └── impl/
│       │       └── K3CloudProxyServiceImpl.java # 服务实现
│       └── util/
│           └── ContoryUtil.java                 # 数据格式转换工具
└── pom.xml
```

## 功能特性

### 1. 登录验证
验证金蝶账号信息，获取可访问的账套列表。

### 2. 元数据查询
获取业务单据的字段结构信息，适配钉钉多维表格式。

### 3. 数据查询
分页查询业务单据数据，支持条件过滤和排序。

## API 接口

### 登录验证
```
POST /middle/kingdee/login
```

**请求参数：**
```json
{
  "SERVER_URL": "http://192.168.1.1/k3cloud",
  "CID": "6882df0b41e0a4",
  "USER_NAME": "admin",
  "APP_ID": "285402_xxxxx",
  "APP_SECRET": "2f9880a4956c42af8df761c6039f803d"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "请求成功",
  "data": [...]
}
```

### 获取表单元数据
```
POST /middle/kingdee/sheetMeta
```

**请求参数：**
```json
{
  "selectedFormId": "PUR_PurchaseOrder",
  "params": {
    "SERVER_URL": "http://192.168.1.1/k3cloud",
    "CID": "6882df0b41e0a4",
    "USER_NAME": "admin",
    "APP_ID": "285402_xxxxx",
    "APP_SECRET": "2f9880a4956c42af8df761c6039f803d"
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "请求成功",
  "data": {
    "Id": "PUR_PurchaseOrder",
    "sheetName": "采购订单",
    "fields": [
      {"id": "FBillNo", "name": "单据编号", "type": "text", "isPrimary": true},
      {"id": "FSpecifier", "name": "规格", "type": "singleSelect", "property": {"choices": [...]}}
    ]
  }
}
```

### 查询数据记录
```
POST /middle/kingdee/records
```

**请求参数：**
```json
{
  "selectedFormId": "PUR_PurchaseOrder",
  "params": {...},
  "maxResults": 20,
  "nextToken": "",
  "filterConditions": [
    {"fieldId": "FBillNo", "operator": "=", "value": "PUR00001"}
  ],
  "sortConfigs": [
    {"fieldId": "FCreatorId", "order": "asc"}
  ]
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "请求成功",
  "data": {
    "nextToken": "20",
    "hasMore": true,
    "records": [
      {
        "id": "PUR00001",
        "fields": {
          "FBillNo": "PUR00001",
          "FSpecifier": "规格值"
        }
      }
    ]
  }
}
```

## 过滤条件操作符

| 操作符 | 说明 | 金蝶API值 |
|--------|------|-----------|
| = | 等于 | 67 |
| < | 小于 | 32 |
| > | 大于 | 39 |
| != | 不等于 | 83 |

## 依赖配置

### 1. pom.xml 依赖
```xml
<dependency>
    <groupId>com.kingdee</groupId>
    <artifactId>k3cloud-webapi</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/k3cloud-webapi-sdk8.0.6.jar</systemPath>
</dependency>
```

### 2. 引入 ding-server 模块
在 `ruoyi-admin/pom.xml` 中添加依赖：
```xml
<dependency>
    <groupId>com.ruoyi</groupId>
    <artifactId>ding-server</artifactId>
</dependency>
```

## 使用示例

### Java 调用示例

```java
// 创建金蝶工具类
KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(
    "http://192.168.1.1/k3cloud",
    "6882df0b41e0a4",
    "admin",
    "285402_xxxxx",
    "2f9880a4956c42af8df761c6039f803d"
);

// 查询数据
JSONObject param = new JSONObject();
param.put("FormId", "PUR_PurchaseOrder");
param.put("FieldKeys", "FBillNo,FBillDate,FSupplierId.FName");
param.put("FilterString", "FBillNo = 'PUR00001'");
param.put("Limit", 20);
List<List<Object>> result = util.query(param);

// 保存单据
Map<String, Object> billData = new HashMap<>();
// ... 设置单据数据
JSONObject saveResult = util.save(billData, "PUR_PurchaseOrder");
```

### 使用过滤条件类

```java
List<KingdeeK3CloudUtil.FilterCondition> conditions = new ArrayList<>();
conditions.add(new KingdeeK3CloudUtil.FilterCondition("FBillNo", KingdeeK3CloudUtil.eq, "PUR00001"));
conditions.add(new KingdeeK3CloudUtil.FilterCondition("FCreatorId", KingdeeK3CloudUtil.dayu, "2025-01-01", 1)); // OR条件

List<Map<String, Object>> result = util.queryDataList("PUR_PurchaseOrder", "FBillNo,FBillDate", conditions);
```

## 注意事项

1. **安全提示**：生产环境中请勿在前端暴露 `APP_SECRET` 等敏感信息
2. **接口权限**：本模块接口已配置为匿名访问（`@Anonymous`）
3. **日期格式**：金蝶接口返回日期格式为 `yyyy-MM-dd'T'HH:mm:ss`
4. **系统依赖**：需要金蝶云星空 WebAPI SDK 支持
