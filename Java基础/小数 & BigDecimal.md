# 小数 & BigDecimal

## 小数

### 定点数

- 纯小数

	- 小数点固定在数值部分的最高位之前，例如 1111 就是 -1*(2^-1*1+2^-2*1+2^-3*1)=-0.875

- 纯整数

	- 小数点固定在数值部分的最后面，例如 1111 表示 -7

### 浮点数

- 精度

	- 单精度

		- float，4个字节32位

	- 双精度

		- double，8个字节64位

	- 延伸单精度
	- 延伸双精度

- 科学记数法（单精度为例）

	- 科学记数法包含：符号、有效数字、指数
	- 符号位：最高二进制位上分配，0 为正 1 为负
	- 尾数，就是有效数字：最右侧分配连续的 23 位用来存储有效数字部分
	- 阶码，也就是指数：符号位右侧分配 8 位存储，二进制转十进制时需要减掉 127，双精度减掉 1023

## BigDecimal（精确运算）

### 通过十进制和小数点解决精度问题

### 主要构造方法

- public BigDecimal(double val) 建议不要使用，因为 Double 本身就不精确
- public BigDecimal(int val)
- public BigDecimal(String val)
- public BigDecimal add(BigDecimal augend)
- public BigDecimal subtract(BigDecimal
subtrahend)
- public BigDecimal multiply(BigDecimal
multiplicand)
- 	
public BigDecimal divide(BigDecimal
divisor)

