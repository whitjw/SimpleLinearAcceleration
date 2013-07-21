SimpleLinearAcceleration
========================

Measure the linear acceleration of your Android device with only the acceleration sensor and no low-pass filters.

Linear Acceleration:

An acceleromter can measure the static gravitation field of earth (like a tilt sensor) or it can measure measure linear acceleration (like accelerating in a vehicle), but it cannot measure both at the same time. When talking about linear acceleration in reference to an acceleration sensor, what we really mean is Linear Acceleration = Measured Acceleration - Gravity. The hard part is determining what part of the signal is gravity.

The Problem:

It is difficult to sequester the gravity component of the signal from the linear acceleration. Some Android devices implement Sensor.TYPE_LINEAR_ACCELERATION and Sensor.TYPE_GRAVITY which perform the calculations for you. Most of these devices are new and equipped with a gyroscope. If you have and older device and do not have a gyroscope, you are going to face some limitations with Sensor.TYPE_ACCELERATION. The tilt of the device can only be measured accurately assuming the device is not experiencing any linear acceleration. The linear acceleration can only be measured accurately if the tilt of the device is known.

A Simple Solution:

One of the most simple solutions to the linear acceleration problem is to measure the orientation of the device while it is static, i.e. not experiencing any angular or linear acceleration. The measured orientation will be used to determine the gravity component of the acceleration measurements until a new measurement can be taken.

![Alt text](http://blog.kircherelectronics.com/blog/images/simple_linear_acceleration_nexus_4_static_tilt.png "Simple Linear Acceleration")
