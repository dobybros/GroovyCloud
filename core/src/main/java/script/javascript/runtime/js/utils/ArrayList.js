var ArrayList = (function ArrayList(){  
      
    _arrayList = function() {
    	this.arr=[];	
    }
    _arrayList.prototype.size=function(){  
        return this.arr.length;  
    }
    _arrayList.prototype.add=function(){  
        if(arguments.length==1){  
            this.arr.push(arguments[0]);  
        }else if(arguments.length>=2){  
            var deleteItem=this.arr[arguments[0]];  
            this.arr.splice(arguments[0],1,arguments[1],deleteItem)  
        }  
        return this;  
    }
    _arrayList.prototype.get=function(index){  
        return this.arr[index];  
    } 
    _arrayList.prototype.removeIndex=function(index){  
    	if(index !== -1)
    		this.arr.splice(index,1);  
    }
    _arrayList.prototype.remove=function(obj){  
        this.removeIndex(this.indexOf(obj));  
    }  
    _arrayList.prototype.indexOf=function(obj){  
        for(var i=0;i<this.arr.length;i++){  
            if (this.arr[i]===obj) {  
                return i;  
            };  
        }  
        return -1;  
    }  
    _arrayList.prototype.isEmpty=function(){  
        return this.arr.length==0;  
    } 
    _arrayList.prototype.clear=function(){  
        this.arr=[];  
    }
//     _arrayList.prototype.copy=function(){
//         var newList = new ArrayList();
//         var arr = this.arr.concat();
//         for(var i = 0; i < arr.length; i++) {
//             newList.add(arr[i]);
//         }
//         return newList;  
//     }
    _arrayList.prototype.contains=function(obj){  
        return this.indexOf(obj)!=-1;  
    }  
    _arrayList.prototype.iterate=function(callback) {
        if(this.arr) {
            if(callback && typeof callback.iterate === 'function') {
            	//Aplomb this.arr may be changed while iterating. 
            	//Aplomb may consider clone array for iterating, but it is the balance of performance. 
                for(var i = 0; i < this.arr.length; i++) {
                    callback.iterate(this.arr[i]);
                }
            } else if(typeof callback === "function") {
                for(var i = 0; i < this.arr.length; i++) {
                    callback(this.arr[i]);
                }
            }
        }
    }
    return _arrayList;
  })();  