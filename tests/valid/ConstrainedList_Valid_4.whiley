define state as {string input, int pos}

char f(state st):
    if(st.pos < |st.input|):
        if isLetter(st.input[st.pos]):
            return st.input[st.pos]
    return ' '

void main([string] args):
    c = f({input:"hello",pos:0})
    println(str(c))
 
