import numpy as np
import matplotlib.pyplot as plt
import math 

def distance(p1, p2):
    return math.sqrt((p1[0]-p2[0])**2+(p1[1]-p2[1])**2)

def brute_force(points): 
    n=len(points)
    #infinitely large value 
    min_dist = float('inf')
    for i in range(0,n-1):
        for j in range (i+1, n): 
            dist = distance(points[i], points[j])
            if dist<min_dist:
                min_dist=dist
                pair = (points[i], points[j])
    return pair, min_dist

def divide_conquer(points_x, points_y): 
    n=len(points_x)
    if n<=3:
        return brute_force(points_x)

    mid=n//2
    mid_x = points_x[mid][0]
    left_x = points_x[:mid]
    right_x = points_x[mid:]
    left_y = []
    right_y = []

    for point in points_y:
        if point[0]<mid_x:
            left_y.append(point)
        else:
            right_y.append(point)

    left_pair, left_dist = divide_conquer(left_x, left_y)
    right_pair, right_dist = divide_conquer(right_x, right_y)

    if left_dist < right_dist:
        closest_pair = left_pair
        closest_dist = left_dist
    else: 
        closest_pair = right_pair
        closest_dist = right_dist

    strip=[]
    for point in points_y: 
        if abs(point[0]-mid_x)<closest_dist:
            strip.append(point)
    strip_pair, strip_dist = closest_strip_pair(strip, closest_dist, closest_pair)
    if strip_dist<closest_dist:
        return strip_pair, strip_dist
    else: 
        return closest_pair, closest_dist

def closest_strip_pair(strip, closest_dist, closest_pair): 
    n = len(strip)
    for i in range (n-1): 
        for j in range (i+1, min(i+6,n)): 
            dist = distance(strip[i], strip[j])
            if dist < closest_dist:
                closest_pair = (strip[i], strip[j])
                closest_dist = dist
    return closest_pair, closest_dist 

np.random.seed(0)
points = np.random.rand(50, 2) * 10

pair, dist =divide_conquer(sorted(points, key=lambda x: x[0]), sorted(points, key=lambda x: x[1]))
print("Closest pair:", pair)
print("Distance:", dist)

# Plot the points and the closest pair
plt.scatter(points[:, 0], points[:, 1])
plt.plot([pair[0][0], pair[1][0]], [pair[0][1], pair[1][1]], 'r')
plt.xlabel('X')
plt.ylabel('Y')
plt.title('Closest Pair of Points')
plt.grid(True)
plt.show()






    




