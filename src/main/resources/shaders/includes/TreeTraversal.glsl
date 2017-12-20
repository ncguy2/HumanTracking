

GeometryItem GetItem(int index);
GeometryItem GetLeftItem(GeometryItem item);
GeometryItem GetRightItem(GeometryItem item);
Geometry GetItemData(GeometryItem item);
float EvaluatePair(float curVal, float next, int operationId, out bool usedLeft);
bool HasData(GeometryItem item);
bool HasValue(GeometryItem item);


Geometry GetItemData(GeometryItem item) {
    return models[item.data];
}

GeometryItem GetLeftItem(GeometryItem item) {
    return GetItem(item.leftItem);
}

GeometryItem GetRightItem(GeometryItem item) {
    return GetItem(item.rightItem);
}

GeometryItem GetItem(int index) {
    return items[index];
}

bool HasData(GeometryItem item) {
    return item.data > 0;
}

float GetValue(int index) {
    return itemValues[index];
}

bool HasValue(int index) {
    return itemsCalculated[index];
}

float EvaluatePair(float curVal, float next, int operationId, out bool usedLeft) {
    switch(operationId) {
        case OPERATION_UNION:
            return UnionSDF(curVal, next, usedLeft);
        case OPERATION_INTERSECTION:
            return IntersectSDF(curVal, next, usedLeft);
        case OPERATION_DIFFERENCE:
            return DifferenceSDF(curVal, next, usedLeft);
    }

    return curVal;
}