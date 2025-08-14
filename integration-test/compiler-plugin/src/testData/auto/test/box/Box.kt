fun box() = "OK"


val a by lazy { 2 <!CAST_NEVER_SUCCEEDS!>as<!> String }