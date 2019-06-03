var HashMap = (function () {
    
    function HashMap() {
        this.clear();
    }

    HashMap.prototype = {
        constructor:HashMap,

        get:function(key) {
            var data = this._data[this.hash(key)];
            return data && data[1];
        },
        
        put:function(key, value) {
            // Store original key as well (for iteration)
            this._data[this.hash(key)] = [key, value];
        },
        
        append:function(key, value) {
            // Store original key as well (for iteration)
            this._data[this.hash(key)] = [key, value];
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
            var keys = [];
            this.forEach(function(value, key) { keys.push(key); });
            return keys;
        },

        values:function() {
            var values = [];
            this.forEach(function(value) { values.push(value); });
            return values;
        },

        count:function() {
            return this.keys().length;
        },

        clear:function() {
            // TODO: Would Object.create(null) make any difference
            this._data = {};
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
                        key._hmuid_ = ++HashMap.uid;
                        hide(key, '_hmuid_');
                    }

                    return '{' + key._hmuid_;
            }
        },

        forEach:function(func) {
            for (var key in this._data) {
                var data = this._data[key];
                func.call(this, data[1], data[0]);
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

    HashMap.uid = 0;

    
    function hide(obj, prop) {
        // Make non iterable if supported
        if (Object.defineProperty) {
            Object.defineProperty(obj, prop, {enumerable:false});
        }
    };

    return HashMap;

})();