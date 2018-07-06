# FinlayScript2

FinlayScript is a JS like scripting language that is suitable for desktop programs and web browsing, this makes it more useful than JS

# Usage:

Debugging:

Simply run the FinlayScript class

Running FinlayScript2 code:

Create a new Program and execute it call

`Program p = new Program();`

`p.libraries.addAll(libs);`

`FinlayScript.interpret(p,scriptFile,scriptDir);`

`p.exec();`

# Writing a script

Note that every declaration or statement must be written on a different line, you cannot have 2 things on the same line.

Comments in FinlayScript are `//` and `;`

To call methods simply write `methodname()` and inside the brackets put any arguments the method takes

Usually the arguments should be seperated with commas but it is left up to the library developer to decide the syntax for methods

You can call methods or use variables in any methods you want as long as the library supports it

At the time of writing this there are 3 built in libraries, IOLib, NetworkingLib and CryptoLib, you can initilize any library that is allowed by the interpretter by calling `<lib> "libraryname"` but some libraries require permissions.

The permissions system in FinlayScript allows it to be suitable for both desktop programs and web development as the interpretter decides what the program has access to, to request permission to use certain methods simply write `<permission> name`.

In FinlayScript there are 3 types of variables but more types can be added with libraries, the 3 types are strings, booleans and ints. Booleans can be initilized with a true or false value, ints can be set to an equasion, number or variable, it will substitute in variables into equasions and follows BEDMAS, for strings you can either use them as `"string here"` or `string here` but the first is the best way because the second one cannot contain variable names in it or it will be substituted.

There are 4 types of methods in FinlayScript void, string, boolean, and int. You declare them like this `type() name{` and then close them with a `}` at the end. Methods can be called by using `name()`. Methods cannot have arguments but all variables are global so they can be stored in a variable before calling the method. You can also call methods in other files by using `<import> "filename" as name` and then you can call `name.method()` or `name.variable`.

You can also use lists, to declare a list simply write `list<type> name`, then to get the size of the list write `name.size()` and to add items to the list use `name.add(item)`

FinlayScript has some primitive logic, you can use while loops, for loops and if statements. For loops take in 3 parameters like this `for(1 : 2 : 3)`, argument 1 is the variable type and name that the variable in the list will be stored in it looks like this `String foo`, argument 2 is the list to be iterated through, and argument 3 is the method to call every time the loop iterates over a item. While loops are also very similar they are used by writing `while(var1,var2,method)` or by using it like this `while(boolean,method)` the first one checks if the two variables are the same and will call the method that is provided until they become different, and the second version calls the method as long as the boolean is equal to TRUE. Then there is the if statement, it is used like this `if(var1,var2,method)` if the two variables are the same then the method is called.

Note that wherever a method takes in a variable you can also provide it with a raw string or boolean.

# Creating a library

Creating libraries is very simple in FinlayScript, simply create a new Java class that extends Library (com.thatmadhacker.finlayscript.Library) and fill in the abstract methods.

In order to register methods that will call the library you will need to call `p.env.methods.add(methodName,this);`

Note that FinlayScript provides an string parsing method, once you have gotten the arguments to your methods simply call `FinlayScript.parseString(arg, program)` (com.thatmadhacker.finlayscript.FinlayScript), do that for all of your arguments and it will return the processed version that substitutes in variables and methods.
