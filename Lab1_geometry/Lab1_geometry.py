import math
import matplotlib.pyplot as plt
import numpy as np

class Vertex: 
    def __init__(self, x ,y):
        self.x = float(x)
        self.y = float(y)
        self.w_in = 0 
        self.w_out = 0 
    def __repr__(self):
        print("("+str(self.x)+";"+str(self.y))
    
class Edge: 
    def __init__(self, start, end):
        self.start = start
        self.end = end
        self.direction = end 
        self.weight = 0
        self.rotation = math.atan2(end.y-start.y,end.x-start.x) #math.atan2(y, x) 
    def __repr__(self):
        print(str(edges.index(self)))

class Point: 
    def __init__(self,x,y):
        self.x=float(x)
        self.y=float(y)
        


def arr_vertex(file_name): 
    vertex = []
    input_arr = open(file_name).read().split()
    n = len(input_arr)
    i=0

    while i<n:
        vertex.append(Vertex(int(input_arr[i]),int(input_arr[i+1])))
        i+=2
    return vertex

def arr_edges(file_name): 
    edges = []
    input_arr = open(file_name).read().split()
    n = len(input_arr)
    i=0

    while i<n:
        edges.append(Edge(vertices[int(input_arr[i])],vertices[int(input_arr[i+1])]))
        i+=2

    return edges

def sum_weight(edges):
    sum=0
    for e in edges: 
        sum+=e.weight
    return sum 

#Sorting edges by the angle relative to the positive axis OX(from leftmost to rightmost)
def sort_edges(array):
    return sorted(array, key=lambda edge: edge.rotation, reverse=True)

def leftedge_unused(arr):
    i=0
    result=arr[0]
    while i<len(arr):
        if arr[i].weight>0:
            result=arr[i]
            break
        i+=1
    return result



def chain(chain_num):
    cur_vertex=0
    while cur_vertex!=n-1:
        next_edge = leftedge_unused(sorted_edges_out[cur_vertex])
        chains[chain_num].append(next_edge)
        next_edge.weight-=1
        cur_vertex=vertices.index(next_edge.end)


def find_point(point): 
    for n in range(0,num_chains,1): 
        for e in chains[n]: 
            if e.start.y<point.y<e.end.y:
                point_vec = Point(point.x-e.start.x, point.y - e.start.y)
                edge_vec = Point(e.end.x - e.start.x, e.end.y-e.start.y)
                if math.atan2(point_vec.y, point_vec.x) > math.atan2(edge_vec.y, edge_vec.x):
                  return "Point is between chains " + str(n - 1) + " , " + str(n)
    return "Point isn`t inside graph"


def print_chains(chain_num): 
    for i in range(0,chain_num):
        print("Chain #"+ str(i))
        for v in chains[i]:
            for k in range(0,n-1):
                if(v.start.x==vertices[k].x and v.start.y==vertices[k].y):
                    print("Vertex"+str(k))
        print("Vertex" +str(n-1))

       
vertices = arr_vertex("vertices.txt")
edges = arr_edges("edges.txt") 
edges_in=[]
edges_out=[]
n=len(vertices)

#Initialization of vertices by y 
vertices = sorted(vertices, key=lambda vertex: vertex.y)

for v in vertices:
    edges_in.insert(vertices.index(v),[])
    edges_out.insert(vertices.index(v),[])

for e in edges:
    index_start = vertices.index(e.start)
    index_end = vertices.index(e.end)
    edges_in[index_end].append(e) 
    edges_out[index_start].append(e)
    e.weight=1

#Configuration of pairs of edges after the first pass
#W_in(v_i)<=W_out(v_i) 
for i in range(1,n-1):
    vertices[i].w_in = sum_weight(edges_in[i])
    vertices[i].w_out = sum_weight(edges_out[i])
    edges_out[i]=sort_edges(edges_out[i])
    if vertices[i].w_in>vertices[i].w_out:
        edges_out[i][0].weight=vertices[i].w_in-vertices[i].w_out+1

#Configuration of pairs of edges after the second pass
#W_in(v_i)=W_out(v_i) 

for i in range (n-1, 0,-1):
    vertices[i].w_in=sum_weight(edges_in[i])
    vertices[i].w_out = sum_weight(edges_out[i])
    edges_in[i]=sort_edges(edges_in[i])
    if vertices[i].w_out>vertices[i].w_in:
        edges_in[i][0].weight=vertices[i].w_out-vertices[i].w_in+edges_in[i][0].weight

#The number of chains is equal to the weight 0 of the vertex
num_chains=sum_weight(edges_out[0])
print(str(num_chains))
chains=[]

sorted_edges_out=[]
for v in edges_out:
    v=sort_edges(v)
    sorted_edges_out.append(v)

#Create chains 
for k in range(num_chains):
    chains.insert(k,[])
    chain(k)

print_chains(num_chains)

point=Point(4,5)
print(find_point(point))


#fig, ax = plt.subplots()
#for edge in edges:
#    x1 = edge.start.x
#    y1 = edge.start.y
#    x2 = edge.end.x
#    y2 = edge.end.y
#    ax.plot([x1,x2], [y1,y2], color='black', linewidth=1, zorder=1)
#for i, vertex in enumerate(vertices):
#    ax.scatter(vertex.x, vertex.y, s=100, color='white', edgecolors='black', linewidth=1, zorder=2)
#    ax.text(vertex.x, vertex.y, str(i), ha='center', va='center', fontsize=12, fontweight='bold', zorder=3)

#plt.show()



def draw_graph(vertices, edges):
    plt.figure(figsize=(8,8))
    for edge in edges:
        x = [edge.start.x, edge.end.x]
        y = [edge.start.y, edge.end.y]
        plt.plot(x, y, 'bo-', linewidth=2, markersize=12)
        dx = edge.end.x - edge.start.x
        dy = edge.end.y - edge.start.y
        dz = np.sqrt(dx*dx + dy*dy)
        if dz != 0:
            plt.arrow(edge.start.x, edge.start.y, dx/dz, dy/dz, head_width=0.2, head_length=0.2, fc='k', ec='k')
    for i, vertex in enumerate(vertices):
        plt.text(vertex.x, vertex.y, str(i), fontsize=14, ha='center', va='center')
   
    plt.scatter(point.x, point.y,s=50)
    plt.show()

draw_graph(vertices, edges)





