int f([int] xs):
    return |xs|

void main([[char]] args):
    left = [1,2,3]
    right = [5,6,7]
    r = f(left + right)
    println(str(r))