function minusFunc(a, b){
	return a-b;
}

function minus() {
	for (var i = 0; i < 1000000; i++) {
//		minusFunc(2, 3);
		addFun(2, 3);
	}
}

function fibonacci1() {
	for(j = 0; j < 10000; j++) {
		var i;
		var fib = []; //Initialize array!
		
		fib[0] = 1;
		fib[1] = 1;
		for(i=2; i<=100; i++)
		{
			fib[i] = fib[i-2] + fib[i-1];
		}
//		fib;
	}
	fib = undefined;
}

function fibonacci() {
	for(j = 0; j < 10000; j++) {
		var i;
		var fib = []; //Initialize array!
		
		fib[0] = 1;
		fib[1] = 1;
		for(i=2; i<=100; i++)
		{
			fib[i] = fib[i-2] + fib[i-1];
		}
//		fib;
	}
	fib = undefined;
}

function fibonacci2() {
	for(j = 0; j < 10000; j++) {
		var i;
		var fib = []; //Initialize array!
		
		fib[0] = 1;
		fib[1] = 1;
		for(i=2; i<=100; i++)
		{
			fib[i] = fib[i-2] + fib[i-1];
		}
//		fib;
	}
	fib = undefined;
}

function fibonacci3() {
	for(j = 0; j < 10000; j++) {
		var i;
		var fib = []; //Initialize array!
		
		fib[0] = 1;
		fib[1] = 1;
		for(i=2; i<=100; i++)
		{
			fib[i] = fib[i-2] + fib[i-1];
		}
//		fib;
	}
	fib = undefined;
}

//var a = "In the above sample we have seen how to use the Java APIs inside the javascript , Now we will see how we can invoke a Java Script snippet or the java script functions inside a java code. The Java8 introduced us few new APIs which we need to use in order to achieve the same.We will be using the “javax.script.ScriptEngineManager” and “javax.script.ScriptEngine” APIs here to invoke/execute javascript functions in java.";
function hello(map) {
//	console.log("typeof " + typeof map);
//	console.log("HashMap " + HashMap);
//	console.log("a " + a);
	fibonacci();
	minus();
	fibonacci1();
	fibonacci2();
	fibonacci3();
	return "dd map.key " + map.key + " map.map.key " + map.map.key + " map.map " + map.map;
}

