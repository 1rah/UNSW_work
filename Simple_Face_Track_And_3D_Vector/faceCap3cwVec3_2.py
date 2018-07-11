import numpy as np
import cv2
#import sys
#import os
import dlib
#import glob
#from skimage import io
#import imutils
#from imutils import face_utils
import time




class faceLandmarkAndPose:
    
    def __init__(self, predictor, video_capture):
        #Obtaining the CAM dimension
        cam_w = int(video_capture.get(3))
        cam_h = int(video_capture.get(4))
        
        TRACKED_POINTS, ALL_POINTS, landmarks_3D = self._initRefPoints()
        camera_matrix, camera_distortion = self._initCamMatrix(cam_w = cam_w, cam_h = cam_h)
        
        def _get(i, pos, frame,
                 camera_matrix = camera_matrix,
                 camera_distortion = camera_distortion,
                 TRACKED_POINTS = TRACKED_POINTS,
                 ALL_POINTS = ALL_POINTS,
                 landmarks_3D = landmarks_3D,
                 predictor = predictor):
            
            return self._markUpFace(i, pos, frame, camera_matrix,
                                    camera_distortion, TRACKED_POINTS,
                                    ALL_POINTS, landmarks_3D, predictor)
        
        self.get = _get
        
    
    def _initCamMatrix(self, cam_w, cam_h):
        #Defining the camera matrix.
        #To have better result it is necessary to find the focal
        # lenght of the camera. fx/fy are the focal lengths (in pixels) 
        # and cx/cy are the optical centres. These values can be obtained 
        # roughly by approximation, for example in a 640x480 camera:
        # cx = 640/2 = 320
        # cy = 480/2 = 240
        # fx = fy = cx/tan(60/2 * pi / 180) = 554.26
        c_x = cam_w / 2
        c_y = cam_h / 2
        f_x = c_x / np.tan(60/2 * np.pi / 180)
        f_y = f_x
        
        #Estimated camera matrix values.
        camera_matrix = np.float32(  [ [f_x, 0.0, c_x],
                                       [0.0, f_y, c_y], 
                                       [0.0, 0.0, 1.0] ])
        
    #    print("Estimated camera matrix: \n" + str(camera_matrix) + "\n")
        
        #Distortion coefficients
        camera_distortion = np.float32([0.0, 0.0, 0.0, 0.0, 0.0])
        
        return camera_matrix, camera_distortion
    
    
    
    def _initRefPoints(self):    
        #Antropometric constant values of the human head. 
        #Found on wikipedia and on:
        # "Head-and-Face Anthropometric Survey of U.S. Respirator Users"
        #
        #X-Y-Z with X pointing forward and Y on the left.
        #The X-Y-Z coordinates used are like the standard
        # coordinates of ROS (robotic operative system)
        P3D_RIGHT_SIDE = np.float32([-100.0, -77.5, -5.0]) #0
        P3D_GONION_RIGHT = np.float32([-110.0, -77.5, -85.0]) #4
        P3D_MENTON = np.float32([0.0, 0.0, -122.7]) #8
        P3D_GONION_LEFT = np.float32([-110.0, 77.5, -85.0]) #12
        P3D_LEFT_SIDE = np.float32([-100.0, 77.5, -5.0]) #16
        P3D_FRONTAL_BREADTH_RIGHT = np.float32([-20.0, -56.1, 10.0]) #17
        P3D_FRONTAL_BREADTH_LEFT = np.float32([-20.0, 56.1, 10.0]) #26
        P3D_SELLION = np.float32([0.0, 0.0, 0.0]) #27
        P3D_NOSE = np.float32([21.1, 0.0, -48.0]) #30
        P3D_SUB_NOSE = np.float32([5.0, 0.0, -52.0]) #33
        P3D_RIGHT_EYE = np.float32([-20.0, -65.5,-5.0]) #36
        P3D_RIGHT_TEAR = np.float32([-10.0, -40.5,-5.0]) #39
        P3D_LEFT_TEAR = np.float32([-10.0, 40.5,-5.0]) #42
        P3D_LEFT_EYE = np.float32([-20.0, 65.5,-5.0]) #45
        #P3D_LIP_RIGHT = np.float32([-20.0, 65.5,-5.0]) #48
        #P3D_LIP_LEFT = np.float32([-20.0, 65.5,-5.0]) #54
        P3D_STOMION = np.float32([10.0, 0.0, -75.0]) #62
        
        #The points to track
        #These points are the ones used by PnP
        # to estimate the 3D pose of the face
        TRACKED_POINTS = (0, 4, 8, 12, 16, 17, 26, 27, 30, 33, 36, 39, 42, 45, 62)
        ALL_POINTS = list(range(0,68)) #Used for debug only
        
        #This matrix contains the 3D points of the
        # 11 landmarks we want to find. It has been
        # obtained from antrophometric measurement
        # on the human head.
        landmarks_3D = np.float32(   [P3D_RIGHT_SIDE,
                                      P3D_GONION_RIGHT,
                                      P3D_MENTON,
                                      P3D_GONION_LEFT,
                                      P3D_LEFT_SIDE,
                                      P3D_FRONTAL_BREADTH_RIGHT,
                                      P3D_FRONTAL_BREADTH_LEFT,
                                      P3D_SELLION,
                                      P3D_NOSE,
                                      P3D_SUB_NOSE,
                                      P3D_RIGHT_EYE,
                                      P3D_RIGHT_TEAR,
                                      P3D_LEFT_TEAR,
                                      P3D_LEFT_EYE,
                                      P3D_STOMION])
        
        return TRACKED_POINTS, ALL_POINTS, landmarks_3D
    
    
    
    def _markUpFace(self, i, pos, frame, camera_matrix, camera_distortion, TRACKED_POINTS, ALL_POINTS, landmarks_3D, predictor):
        face_x1 = pos.left()
        face_y1 = pos.top()
        face_x2 = pos.right()
        face_y2 = pos.bottom()
        text_x1 = face_x1
        text_y1 = face_y1 - 3
        
#        cv2.putText(frame, "FACE " + str(i+1), (text_x1,text_y1), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0,255,0), 1)
#        cv2.rectangle(frame, (face_x1, face_y1), (face_x2, face_y2), (0, 255, 0), 2)            
        
        
        #Creating a dlib rectangle and finding the landmarks
        dlib_rectangle = dlib.rectangle(face_x1, face_y1, face_x2, face_y2)
        dlib_landmarks = predictor(frame, dlib_rectangle)
        
        #It selects only the landmarks that
        # have been indicated in the input parameter "points_to_return".
        #It can be used in solvePnP() to estimate the 3D pose.
        
        landmarks_2D = np.zeros((len(TRACKED_POINTS),2), dtype=np.float32)
        counter = 0
        for point in ALL_POINTS:
            x, y = dlib_landmarks.parts()[point].x, dlib_landmarks.parts()[point].y
            
            if point in TRACKED_POINTS:
                landmarks_2D[counter] = [x, y]
                cv2.circle(frame,(x, y), 4, (0,0,255), -1)
                counter += 1
                
            cv2.circle(frame,(x, y), 2, (0,255,0), -1)
        
        
        #Applying the PnP solver to find the 3D pose
        # of the head from the 2D position of the
        # landmarks.
        #retval - bool
        #rvec - Output rotation vector that, together with tvec, brings 
        # points from the model coordinate system to the camera coordinate system.
        #tvec - Output translation vector.
        retval, rvec, tvec = cv2.solvePnP(landmarks_3D, 
                                              landmarks_2D, 
                                              camera_matrix, camera_distortion)
        
        #Now we project the 3D points into the image plane
        #Creating a 3-axis to be used as reference in the image.
        axis = np.float32(       [[50,0,0], 
                                  [0,50,0], 
                                  [0,0,50]])
        imgpts, jac = cv2.projectPoints(axis, rvec, tvec, camera_matrix, camera_distortion)
        
        #Drawing the three axis on the image frame.
        #The opencv colors are defined as BGR colors such as: 
        # (a, b, c) >> Blue = a, Green = b and Red = c
        #Our axis/color convention is X=R, Y=G, Z=B
        sellion_xy = (landmarks_2D[7][0], landmarks_2D[7][1])
        cv2.line(frame, sellion_xy, tuple(imgpts[1].ravel()), (0,255,0), 3) #GREEN
        cv2.line(frame, sellion_xy, tuple(imgpts[2].ravel()), (255,0,0), 3) #BLUE
        cv2.line(frame, sellion_xy, tuple(imgpts[0].ravel()), (0,0,255), 3) #RED
        
        return frame





# init imported functions
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor('./shape_predictor_68_face_landmarks.dat')
video_capture = cv2.VideoCapture(0)

# init the Face Landmark and Pose Detector
flp = faceLandmarkAndPose(predictor, video_capture)

#Create the main window and move it
cv2.namedWindow('Video')
cv2.moveWindow('Video', 20, 20)

start = time.time()
num_frames = 0
while(video_capture.isOpened() == True):

    # Capture frame-by-frame
    ret, frame = video_capture.read()

    faces_array = detector(frame, 1)
    print("Total Faces: " + str(len(faces_array)))
    
    original = frame.copy()
    for i, pos in enumerate(faces_array):
        frame = flp.get(i, pos, frame)
    
    alpha = 0.5
    output = cv2.addWeighted(frame, alpha, original, 1 - alpha, 0)
    cv2.imshow('Video', frame)
    
    num_frames += 1
    seconds = time.time() - start		
    fps  = num_frames / seconds;
    print("Estimated frames per second : {0}".format(fps))  
    
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything done, release the capture
video_capture.release()
cv2.destroyAllWindows()
for i in range(5):
    cv2.waitKey(1)