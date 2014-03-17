package org.flightclub;/*
	tell camera where to stick itself
	24 sep 2001
*/

interface CameraSubject {
    public Vector3d getEye();

    public Vector3d getFocus();
}