(int,int) f(int x):
    return (x,x+2)

void main([[char]] args):
    (x,y) = f(1)
    println(str(x))
    println(str(y))