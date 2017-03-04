# Test Cases

## Construct Identity

new port
has text => infers type 'text'
has text : text => infers type 'text' from constraint list and accepts any text assignment
    * has text text : text => INVALID?
	* has f1 => type should be ALL
has f2 text
has f3 : 'foo'
has f4 : 'foo', 'bar' => f4 infers type 'text' and limits domain to 'foo' and 'bar'
has f5 text : 'foo'
has f6 text : 'foo', 'bar'
	* has f7 text : foo => ERROR: undefined type
has f8 text : port
    * has f9 text : text => INVALID?

has f10 list
has f11 : list
    ? has f12 list : list => f12 list contains lists
has f13 list : port
has f14 list : text
has f15 list : 'foo'
has f16 list : 'foo', 'bar'
has f17 list : 'foo', port => infers listTypes TEXT and PORT, text domain 'foo'
    * has f-18 list : port(uuid:<uuid>), port(uuid:<uuid>)
    
*NOTE*: ':' symbol means "can be assigned" for non-list types and accepts examples/literals; and "can contain" for list and accepts both identifier types and examples/literals, inferring types

	* has list
has port list
has port

? has f30 : 'foo', port
has f31 : port
has f31 text : 'foo', port

## Construct Instance

new port
has mode
has direction
has voltage
add port
set mode : 'digital'
set direction : 'input'
set voltage : 'cmos'

new port
has mode : 'none', 'digital', 'analog', 'pwm', 'resistive-touch', 'power', 'i2c(scl)', 'i2c(sda)', 'spi(sclk)', 'spi(mosi)', 'spi(miso)', 'spi(ss)', 'uart(rx)', 'uart(tx)'
has direction : 'none', 'input', 'output', 'bidirectional'
has voltage : 'none', 'ttl', 'cmos'
add port
set mode : 'digital'
set direction : 'input'
set voltage : 'cmos'

new port
has mode : 'none', 'digital', 'analog', 'pwm', 'resistive-touch', 'power', 'i2c(scl)', 'i2c(sda)', 'spi(sclk)', 'spi(mosi)', 'spi(miso)', 'spi(ss)', 'uart(rx)', 'uart(tx)'
has direction : 'none', 'input', 'output', 'bidirectional'
has voltage : 'none', 'ttl', 'cmos'
add port
let mode:none;direction:null;voltage:null
let mode:digital;direction:input,output,bidirectional;voltage:ttl,cmos
let mode:analog;direction:input,output;voltage:ttl,cmos
let mode:pwm;direction:input,output;voltage:ttl,cmos
let mode:resistive_touch;direction:input;voltage:null
let mode:power;direction:output;voltage:ttl,cmos
let mode:power;direction:input;voltage:common
let mode:i2c(scl);direction:bidirectional;voltage:cmos
let mode:i2c(sda);direction:bidirectional;voltage:cmos
let mode:spi(sclk);direction:output;voltage:ttl,cmos
let mode:spi(mosi);direction:output;voltage:ttl,cmos
let mode:spi(miso);direction:input;voltage:ttl,cmos
let mode:spi(ss);direction:output;voltage:ttl,cmos
let mode:uart(rx);direction:input;voltage:ttl,cmos
let mode:uart(tx);direction:output;voltage:ttl,cmos
set mode : 'digital'
set direction : 'input'
set voltage : 'cmos'
