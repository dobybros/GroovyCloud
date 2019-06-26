function addFun(a, b){
	return a+b;
}

function add() {
	for (var i = 0; i < 1000000; i++) {
		addFun(2, 3);
	}
}

function hi() {
	print('hi method is called ');
	return "cc";
}

function map() {
	return {"h" : "aaa", "b" : 123, "c" : [1,12,2]};
}

function array() {
	return [1,12,2];
}