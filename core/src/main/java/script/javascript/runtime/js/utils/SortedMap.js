var SortedMap = (function () {
    
    function SortedMap() {
        this.keys = new ArrayList();
        this.clear();
    }

    SortedMap.prototype = {
        constructor:SortedMap,

        get:function(key) {
            var data = this._data[this.hash(key)];
            return data && data[1];
        },
        
        put:function(key, value) {
            // Store original key as well (for iteration)
            this._data[this.hash(key)] = [key, value];
            this.keys.add(key);
        },
        
        append:function(key, value) {
            // Store original key as well (for iteration)
            this.put(key, value);
            return this;
        },

        has:function(key) {
            return this.hash(key) in this._data;
        },
        
        search:function(value) {
            for (var key in this._data) {
                if (this._data[key][1] === value) {
                    return this._data[key][0];
                }
            }

            return null;
        },
        
        remove:function(key) {
            delete this._data[this.hash(key)];
             this.keys.remove(key);
        },

        type:function(key) {
            var str = Object.prototype.toString.call(key);
            var type = str.slice(8, -1).toLowerCase();
            // Some browsers yield DOMWindow for null and undefined, works fine on Node
            if (type === 'domwindow' && !key) {
                return key + '';
            }
            return type;
        },

        keys:function() {
            return this.keys.arr;
        },

        values:function() {
            var values = [];
            this.keys.iterate(S$.call(this, function(key){
                values.push(this.get(key));
            }));
            
            return values;
        },

        count:function() {
            return this.keys.size();
        },

        clear:function() {
            // TODO: Would Object.create(null) make any difference
            this._data = {};
            this.keys.clear();
        },

        hash:function(key) {
            switch (this.type(key)) {
                case 'undefined':
                case 'null':
                case 'boolean':
                case 'number':
                case 'regexp':
                    return key + '';

                case 'date':
                    return ':' + key.getTime();

                case 'string':
                    return '"' + key;

                case 'array':
                    var hashes = [];
                    for (var i = 0; i < key.length; i++)
                        hashes[i] = this.hash(key[i]);
                    return '[' + hashes.join('|');

                case 'object':
                default:
                    // TODO: Don't use expandos when Object.defineProperty is not available?
                    if (!key._hmuid_) {
                        key._hmuid_ = ++SortedMap.uid;
                        hide(key, '_hmuid_');
                    }

                    return '{' + key._hmuid_;
            }
        },

        forEach:function(func) {
            if(func !== undefined) {
                this.keys.iterate(S$.call(this, function(key){
                    var value = this.get(key);
                    func.call(this, value, key);
                }));   
            }
        }, 

        copy:function(map) {
            if(map !== undefined) {
                map.forEach(S$.call(this, function(value, key) {
                    this.put(key, value);
                }));
            }
        }
    };

    SortedMap.uid = 0;

    
    function hide(obj, prop) {
        // Make non iterable if supported
        if (Object.defineProperty) {
            Object.defineProperty(obj, prop, {enumerable:false});
        }
    };

    return SortedMap;

})();