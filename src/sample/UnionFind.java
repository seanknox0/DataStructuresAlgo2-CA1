package sample;

public class UnionFind {

    public static void union(int[] a, int p, int q) {
        a[find(a,q)]=find(a,p); //The root of q is made reference the root of p
    }

    //Iterative version of find with path compression
    public static int find(int[] a, int id) {
        if(a[id] == -1){
            return -1;
        }
            while(a[id]!=id) {
                a[id]=a[a[id]]; //Compress path
                id=a[id];
            }
        return id;
    }
}
