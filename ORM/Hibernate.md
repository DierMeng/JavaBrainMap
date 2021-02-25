# Hibernate

## 注解汇总

### @MappedSuperclass

- 基于代码复用和模型分离的思想，在项目开发中使用 JPA 的 @MappedSuperclass 注解将实体类的多个属性分别封装到不同的非实体类中。
- 只能标注在类上
- 标注为 @MappedSuperclass 的类将不是一个完整的实体类。

	- 不会映射到数据库表，但是它的属性都将映射到其子类的数据库表字段中

- 标注为 @MappedSuperclass 的类不能再标注 @Entity 或 @Table 注解，也无需实现序列化接口。

	- 如果一个标注为 @MappedSuperclass 的类继承了另外一个实体类或者另外一个同样标注了 @MappedSuperclass 的类的话，他将可以使用 @AttributeOverride 或 @AttributeOverrides 注解重定义其父类(无论是否是实体类)的属性映射到数据库表中的字段。

