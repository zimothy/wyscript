string f({int} xs, {int} ys):
    if xs ⊆ ys:
        return "XS IS A SUBSET"
    else:
        return "XS IS NOT A SUBSET"

void main([string] args):
    println(f({1,2,3},{1,2,3}))
    println(f({1,4},{1,2,3}))
    println(f({1},{1,2,3}))
