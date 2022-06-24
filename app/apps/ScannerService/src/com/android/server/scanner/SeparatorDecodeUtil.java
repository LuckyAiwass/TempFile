package com.android.server.scanner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.device.scanner.configuration.Symbology;

public class SeparatorDecodeUtil {
	
	private static final int STARTSWITH2 = 2;
	private static final int STARTSWITH3 = 3;
	private static final int STARTSWITH4 = 4;
	private static byte[] setSeparatorChar = new byte[]{0x28, 0x29};
	/*
	 * STARTSEPARATOR values is ASCII
	 * string startWith STARTSEPARATORTABLE[i][j]
	 */
	private static final byte[][] STARTSEPARATORTABLE = {
		{0x30 , 0x30}, // 00
		{0x30 , 0x31}, // 01
		{0x30 , 0x32}, // 02
		{0x30 , 0x33}, // 03*
		{0x30 , 0x34}, // 04*
		{0x31 , 0x30}, // 10
		{0x31 , 0x31}, // 11
		{0x31 , 0x32}, // 12
		{0x31 , 0x33}, // 13
		{0x31 , 0x34}, // 14
		{0x31 , 0x35}, // 15
		{0x31 , 0x36}, // 16
		{0x31 , 0x37}, // 17
		{0x31 , 0x38}, // 18*
		{0x31 , 0x39}, // 19*
		{0x32 , 0x30}, // 20
		{0x32 , 0x31}, // 21
		{0x32 , 0x32}, // 22
		{0x33 , 0x30}, // 30
		{0x33 , 0x31}, // 31
		{0x33 , 0x32}, // 32
		{0x33 , 0x33}, // 33
		{0x33 , 0x34}, // 34
		{0x33 , 0x35}, // 35
		{0x33 , 0x36}, // 36
		{0x33 , 0x37}, // 37
		{0x34 , 0x31}, // 41
		{0x39 , 0x30}, // 90
		{0x39 , 0x31}, // 91
		{0x39 , 0x32}, // 92
		{0x39 , 0x33}, // 93
		{0x39 , 0x34}, // 94
		{0x39 , 0x35}, // 95
		{0x39 , 0x36}, // 96
		{0x39 , 0x37}, // 97
		{0x39 , 0x38}, // 98
		{0x39 , 0x39}  // 99
	};
	
	/*
	 * STARTSEPARATORTABLELENTH values is STARTSEPARATORTABLE[i] length
	 * STARTSEPARATORTABLE[i].length value is STARTSEPARATORTABLELENGTH[i]
	 */
	private static final int[] STARTSEPARATORTABLELENGTH = {
		20, // 00 max length is 20
		16, // 01 max length is 16
		16, // 02
		16, // 03*
		18, // 04*
		22, // 10
		8,  // 11
		8,  // 12
		8,  // 13
		8,  // 14
		8,  // 15
		8,  // 16
		8,  // 17
		8,  // 18*
		8,  // 19*
		4,  // 20
		22, // 21
		31, // 22
		8,  // 30
		10, // 31
		10, // 32
		10, // 33
		10, // 34
		10, // 35
		10, // 36
		10, // 37
		16, // 41
		32, // 90
		32, // 91
		32, // 92
		32, // 93
		32, // 94
		32, // 95
		32, // 96
		32, // 97
		32, // 98
		32  // 99
	};

	private static final byte[][] STARTSEPARATORTABLE3 = {
		{0x32 , 0x34 , 0x30}, // ASCII value 240
		{0x32 , 0x34 , 0x31}, // ASCII value 241
		{0x32 , 0x34 , 0x32}, // ASCII value 242
		{0x32 , 0x35 , 0x30}, // ASCII value 250
		{0x32 , 0x35 , 0x31}, // ASCII value 251
		{0x32 , 0x35 , 0x33}, // ASCII value 253
		{0x32 , 0x35 , 0x34}, // ASCII value 254
		{0x34 , 0x30 , 0x30}, // ASCII value 400
		{0x34 , 0x30 , 0x31}, // ASCII value 401
		{0x34 , 0x30 , 0x32}, // ASCII value 402
		{0x34 , 0x30 , 0x33}, // ASCII value 403
		{0x34 , 0x31 , 0x30}, // ASCII value 410
		{0x34 , 0x31 , 0x31}, // ASCII value 411
		{0x34 , 0x31 , 0x32}, // ASCII value 412
		{0x34 , 0x31 , 0x33}, // ASCII value 413
		{0x34 , 0x31 , 0x34}, // ASCII value 414
		{0x34 , 0x31 , 0x35}, // ASCII value 415
		{0x34 , 0x32 , 0x30}, // ASCII value 420
		{0x34 , 0x32 , 0x31}, // ASCII value 421
		{0x34 , 0x32 , 0x32}, // ASCII value 422
		{0x34 , 0x32 , 0x33}, // ASCII value 423
		{0x34 , 0x32 , 0x34}, // ASCII value 424
		{0x34 , 0x32 , 0x35}, // ASCII value 425
		{0x34 , 0x32 , 0x36}  // ASCII value 426
	};

	private static final int[] STARTSEPARATORTABLELENGTH3 = {
		33, // 240 max length is 33
		33, // 241 max length is 33
		9,  // 242 max length is 9
		33, // 250
		33, // 251
		33, // 253
		23, // 254
		33, // 400
		33, // 401
		20, // 402
		33, // 403
		16, // 410
		16, // 411
		16, // 412
		16, // 413
		16, // 414
		16, // 415
		23, // 420
		18, // 421
		6,  // 422
		18, // 423
		6,  // 424
		6,  // 425
		6   // 426
	};

	private static final byte[][] STARTSEPARATORTABLE4 = {
		{0x33 , 0x31 , 0x30 , 0x30}, // ASCII value 3100
		{0x33 , 0x31 , 0x30 , 0x31}, // ASCII value 3101
		{0x33 , 0x31 , 0x30 , 0x32}, // ASCII value 3102
		{0x33 , 0x31 , 0x30 , 0x33}, // ASCII value 3103
		{0x33 , 0x31 , 0x30 , 0x34}, // ASCII value 3104
		{0x33 , 0x31 , 0x30 , 0x35}, // ASCII value 3105
		{0x33 , 0x31 , 0x30 , 0x36}, // ASCII value 3106
		{0x33 , 0x31 , 0x30 , 0x37}, // ASCII value 3107
		{0x33 , 0x31 , 0x30 , 0x38}, // ASCII value 3108
		{0x33 , 0x31 , 0x30 , 0x39}, // ASCII value 3109
		{0x33 , 0x31 , 0x31 , 0x30}, // ASCII value 3110
		{0x33 , 0x31 , 0x31 , 0x31}, // ASCII value 3111
		{0x33 , 0x31 , 0x31 , 0x32}, // ASCII value 3112
		{0x33 , 0x31 , 0x31 , 0x33}, // ASCII value 3113
		{0x33 , 0x31 , 0x31 , 0x34}, // ASCII value 3114
		{0x33 , 0x31 , 0x31 , 0x35}, // ASCII value 3115
		{0x33 , 0x31 , 0x31 , 0x36}, // ASCII value 3116
		{0x33 , 0x31 , 0x31 , 0x37}, // ASCII value 3117
		{0x33 , 0x31 , 0x31 , 0x38}, // ASCII value 3118
		{0x33 , 0x31 , 0x31 , 0x39}, // ASCII value 3119
		{0x33 , 0x31 , 0x32 , 0x30}, // ASCII value 3120
		{0x33 , 0x31 , 0x32 , 0x31}, // ASCII value 3121
		{0x33 , 0x31 , 0x32 , 0x32}, // ASCII value 3122
		{0x33 , 0x31 , 0x32 , 0x33}, // ASCII value 3123
		{0x33 , 0x31 , 0x32 , 0x34}, // ASCII value 3124
		{0x33 , 0x31 , 0x32 , 0x35}, // ASCII value 3125
		{0x33 , 0x31 , 0x32 , 0x36}, // ASCII value 3126
		{0x33 , 0x31 , 0x32 , 0x37}, // ASCII value 3127
		{0x33 , 0x31 , 0x32 , 0x38}, // ASCII value 3128
		{0x33 , 0x31 , 0x32 , 0x39}, // ASCII value 3129
		{0x33 , 0x31 , 0x33 , 0x30}, // ASCII value 3130
		{0x33 , 0x31 , 0x33 , 0x31}, // ASCII value 3131
		{0x33 , 0x31 , 0x33 , 0x32}, // ASCII value 3132
		{0x33 , 0x31 , 0x33 , 0x33}, // ASCII value 3133
		{0x33 , 0x31 , 0x33 , 0x34}, // ASCII value 3134
		{0x33 , 0x31 , 0x33 , 0x35}, // ASCII value 3135
		{0x33 , 0x31 , 0x33 , 0x36}, // ASCII value 3136
		{0x33 , 0x31 , 0x33 , 0x37}, // ASCII value 3137
		{0x33 , 0x31 , 0x33 , 0x37}, // ASCII value 3138
		{0x33 , 0x31 , 0x33 , 0x39}, // ASCII value 3139
		{0x33 , 0x31 , 0x34 , 0x30}, // ASCII value 3140
		{0x33 , 0x31 , 0x34 , 0x31}, // ASCII value 3141
		{0x33 , 0x31 , 0x34 , 0x32}, // ASCII value 3142
		{0x33 , 0x31 , 0x34 , 0x33}, // ASCII value 3143
		{0x33 , 0x31 , 0x34 , 0x34}, // ASCII value 3144
		{0x33 , 0x31 , 0x34 , 0x35}, // ASCII value 3145
		{0x33 , 0x31 , 0x34 , 0x36}, // ASCII value 3146
		{0x33 , 0x31 , 0x34 , 0x37}, // ASCII value 3147
		{0x33 , 0x31 , 0x34 , 0x38}, // ASCII value 3148
		{0x33 , 0x31 , 0x34 , 0x39}, // ASCII value 3149
		{0x33 , 0x31 , 0x35 , 0x30}, // ASCII value 3150
		{0x33 , 0x31 , 0x35 , 0x31}, // ASCII value 3151
		{0x33 , 0x31 , 0x35 , 0x32}, // ASCII value 3152
		{0x33 , 0x31 , 0x35 , 0x33}, // ASCII value 3153
		{0x33 , 0x31 , 0x35 , 0x34}, // ASCII value 3154
		{0x33 , 0x31 , 0x35 , 0x35}, // ASCII value 3155
		{0x33 , 0x31 , 0x35 , 0x36}, // ASCII value 3156
		{0x33 , 0x31 , 0x35 , 0x37}, // ASCII value 3157
		{0x33 , 0x31 , 0x35 , 0x38}, // ASCII value 3158
		{0x33 , 0x31 , 0x35 , 0x39}, // ASCII value 3159
		{0x33 , 0x31 , 0x36 , 0x30}, // ASCII value 3160
		{0x33 , 0x31 , 0x36 , 0x31}, // ASCII value 3161
		{0x33 , 0x31 , 0x36 , 0x32}, // ASCII value 3162
		{0x33 , 0x31 , 0x36 , 0x33}, // ASCII value 3163
		{0x33 , 0x31 , 0x36 , 0x34}, // ASCII value 3164
		{0x33 , 0x31 , 0x36 , 0x35}, // ASCII value 3165
		{0x33 , 0x31 , 0x36 , 0x36}, // ASCII value 3166
		{0x33 , 0x31 , 0x36 , 0x37}, // ASCII value 3167
		{0x33 , 0x31 , 0x36 , 0x38}, // ASCII value 3168
		{0x33 , 0x31 , 0x36 , 0x39}, // ASCII value 3169
		{0x33 , 0x32 , 0x30 , 0x30}, // ASCII value 3200
		{0x33 , 0x32 , 0x30 , 0x31}, // ASCII value 3201
		{0x33 , 0x32 , 0x30 , 0x32}, // ASCII value 3202
		{0x33 , 0x32 , 0x30 , 0x33}, // ASCII value 3203
		{0x33 , 0x32 , 0x30 , 0x34}, // ASCII value 3204
		{0x33 , 0x32 , 0x30 , 0x35}, // ASCII value 3205
		{0x33 , 0x32 , 0x30 , 0x36}, // ASCII value 3206
		{0x33 , 0x32 , 0x30 , 0x37}, // ASCII value 3207
		{0x33 , 0x32 , 0x30 , 0x38}, // ASCII value 3208
		{0x33 , 0x32 , 0x30 , 0x39}, // ASCII value 3209
		{0x33 , 0x32 , 0x31 , 0x30}, // ASCII value 3210
		{0x33 , 0x32 , 0x31 , 0x31}, // ASCII value 3211
		{0x33 , 0x32 , 0x31 , 0x32}, // ASCII value 3212
		{0x33 , 0x32 , 0x31 , 0x33}, // ASCII value 3213
		{0x33 , 0x32 , 0x31 , 0x34}, // ASCII value 3214
		{0x33 , 0x32 , 0x31 , 0x35}, // ASCII value 3215
		{0x33 , 0x32 , 0x31 , 0x36}, // ASCII value 3216
		{0x33 , 0x32 , 0x31 , 0x37}, // ASCII value 3217
		{0x33 , 0x32 , 0x31 , 0x38}, // ASCII value 3218
		{0x33 , 0x32 , 0x31 , 0x39}, // ASCII value 3219
		{0x33 , 0x32 , 0x32 , 0x30}, // ASCII value 3220
		{0x33 , 0x32 , 0x32 , 0x31}, // ASCII value 3221
		{0x33 , 0x32 , 0x32 , 0x32}, // ASCII value 3222
		{0x33 , 0x32 , 0x32 , 0x33}, // ASCII value 3223
		{0x33 , 0x32 , 0x32 , 0x34}, // ASCII value 3224
		{0x33 , 0x32 , 0x32 , 0x35}, // ASCII value 3225
		{0x33 , 0x32 , 0x32 , 0x36}, // ASCII value 3226
		{0x33 , 0x32 , 0x32 , 0x37}, // ASCII value 3227
		{0x33 , 0x32 , 0x32 , 0x38}, // ASCII value 3228
		{0x33 , 0x32 , 0x32 , 0x39}, // ASCII value 3229
		{0x33 , 0x32 , 0x33 , 0x30}, // ASCII value 3230
		{0x33 , 0x32 , 0x33 , 0x31}, // ASCII value 3231
		{0x33 , 0x32 , 0x33 , 0x32}, // ASCII value 3232
		{0x33 , 0x32 , 0x33 , 0x33}, // ASCII value 3233
		{0x33 , 0x32 , 0x33 , 0x34}, // ASCII value 3234
		{0x33 , 0x32 , 0x33 , 0x35}, // ASCII value 3235
		{0x33 , 0x32 , 0x33 , 0x36}, // ASCII value 3236
		{0x33 , 0x32 , 0x33 , 0x37}, // ASCII value 3237
		{0x33 , 0x32 , 0x33 , 0x38}, // ASCII value 3238
		{0x33 , 0x32 , 0x33 , 0x39}, // ASCII value 3239
		{0x33 , 0x32 , 0x34 , 0x30}, // ASCII value 3240
		{0x33 , 0x32 , 0x34 , 0x31}, // ASCII value 3241
		{0x33 , 0x32 , 0x34 , 0x32}, // ASCII value 3242
		{0x33 , 0x32 , 0x34 , 0x33}, // ASCII value 3243
		{0x33 , 0x32 , 0x34 , 0x34}, // ASCII value 3244
		{0x33 , 0x32 , 0x34 , 0x35}, // ASCII value 3245
		{0x33 , 0x32 , 0x34 , 0x36}, // ASCII value 3246
		{0x33 , 0x32 , 0x34 , 0x37}, // ASCII value 3247
		{0x33 , 0x32 , 0x34 , 0x38}, // ASCII value 3248
		{0x33 , 0x32 , 0x34 , 0x39}, // ASCII value 3249
		{0x33 , 0x32 , 0x35 , 0x30}, // ASCII value 3250
		{0x33 , 0x32 , 0x35 , 0x31}, // ASCII value 3251
		{0x33 , 0x32 , 0x35 , 0x32}, // ASCII value 3252
		{0x33 , 0x32 , 0x35 , 0x33}, // ASCII value 3253
		{0x33 , 0x32 , 0x35 , 0x34}, // ASCII value 3254
		{0x33 , 0x32 , 0x35 , 0x35}, // ASCII value 3255
		{0x33 , 0x32 , 0x35 , 0x36}, // ASCII value 3256
		{0x33 , 0x32 , 0x35 , 0x37}, // ASCII value 3257
		{0x33 , 0x32 , 0x35 , 0x38}, // ASCII value 3258
		{0x33 , 0x32 , 0x35 , 0x39}, // ASCII value 3259
		{0x33 , 0x32 , 0x36 , 0x30}, // ASCII value 3260
		{0x33 , 0x32 , 0x36 , 0x31}, // ASCII value 3261
		{0x33 , 0x32 , 0x36 , 0x32}, // ASCII value 3262
		{0x33 , 0x32 , 0x36 , 0x33}, // ASCII value 3263
		{0x33 , 0x32 , 0x36 , 0x34}, // ASCII value 3264
		{0x33 , 0x32 , 0x36 , 0x35}, // ASCII value 3265
		{0x33 , 0x32 , 0x36 , 0x36}, // ASCII value 3266
		{0x33 , 0x32 , 0x36 , 0x37}, // ASCII value 3267
		{0x33 , 0x32 , 0x36 , 0x38}, // ASCII value 3268
		{0x33 , 0x32 , 0x36 , 0x39}, // ASCII value 3269
		{0x33 , 0x32 , 0x37 , 0x30}, // ASCII value 3270
		{0x33 , 0x32 , 0x37 , 0x31}, // ASCII value 3271
		{0x33 , 0x32 , 0x37 , 0x32}, // ASCII value 3272
		{0x33 , 0x32 , 0x37 , 0x33}, // ASCII value 3273
		{0x33 , 0x32 , 0x37 , 0x34}, // ASCII value 3274
		{0x33 , 0x32 , 0x37 , 0x35}, // ASCII value 3275
		{0x33 , 0x32 , 0x37 , 0x36}, // ASCII value 3276
		{0x33 , 0x32 , 0x37 , 0x37}, // ASCII value 3277
		{0x33 , 0x32 , 0x37 , 0x38}, // ASCII value 3278
		{0x33 , 0x32 , 0x37 , 0x39}, // ASCII value 3279
		{0x33 , 0x32 , 0x38 , 0x30}, // ASCII value 3280
		{0x33 , 0x32 , 0x38 , 0x31}, // ASCII value 3281
		{0x33 , 0x32 , 0x38 , 0x32}, // ASCII value 3282
		{0x33 , 0x32 , 0x38 , 0x33}, // ASCII value 3283
		{0x33 , 0x32 , 0x38 , 0x34}, // ASCII value 3284
		{0x33 , 0x32 , 0x38 , 0x35}, // ASCII value 3285
		{0x33 , 0x32 , 0x38 , 0x36}, // ASCII value 3286
		{0x33 , 0x32 , 0x38 , 0x37}, // ASCII value 3287
		{0x33 , 0x32 , 0x38 , 0x38}, // ASCII value 3288
		{0x33 , 0x32 , 0x38 , 0x39}, // ASCII value 3289
		{0x33 , 0x32 , 0x39 , 0x30}, // ASCII value 3290
		{0x33 , 0x32 , 0x39 , 0x31}, // ASCII value 3291
		{0x33 , 0x32 , 0x39 , 0x32}, // ASCII value 3292
		{0x33 , 0x32 , 0x39 , 0x33}, // ASCII value 3293
		{0x33 , 0x32 , 0x39 , 0x34}, // ASCII value 3294
		{0x33 , 0x32 , 0x39 , 0x35}, // ASCII value 3295
		{0x33 , 0x32 , 0x39 , 0x36}, // ASCII value 3296
		{0x33 , 0x32 , 0x39 , 0x37}, // ASCII value 3297
		{0x33 , 0x32 , 0x39 , 0x38}, // ASCII value 3298
		{0x33 , 0x32 , 0x39 , 0x39}, // ASCII value 3299
		{0x33 , 0x33 , 0x30 , 0x30}, // ASCII value 3300
		{0x33 , 0x33 , 0x30 , 0x31}, // ASCII value 3301
		{0x33 , 0x33 , 0x30 , 0x32}, // ASCII value 3302
		{0x33 , 0x33 , 0x30 , 0x33}, // ASCII value 3303
		{0x33 , 0x33 , 0x30 , 0x34}, // ASCII value 3304
		{0x33 , 0x33 , 0x30 , 0x35}, // ASCII value 3305
		{0x33 , 0x33 , 0x30 , 0x36}, // ASCII value 3306
		{0x33 , 0x33 , 0x30 , 0x37}, // ASCII value 3307
		{0x33 , 0x33 , 0x30 , 0x38}, // ASCII value 3308
		{0x33 , 0x33 , 0x30 , 0x39}, // ASCII value 3309
		{0x33 , 0x33 , 0x31 , 0x30}, // ASCII value 3310
		{0x33 , 0x33 , 0x31 , 0x31}, // ASCII value 3311
		{0x33 , 0x33 , 0x31 , 0x32}, // ASCII value 3312
		{0x33 , 0x33 , 0x31 , 0x33}, // ASCII value 3313
		{0x33 , 0x33 , 0x31 , 0x34}, // ASCII value 3314
		{0x33 , 0x33 , 0x31 , 0x35}, // ASCII value 3315
		{0x33 , 0x33 , 0x31 , 0x36}, // ASCII value 3316
		{0x33 , 0x33 , 0x31 , 0x37}, // ASCII value 3317
		{0x33 , 0x33 , 0x31 , 0x38}, // ASCII value 3318
		{0x33 , 0x33 , 0x31 , 0x39}, // ASCII value 3319
		{0x33 , 0x33 , 0x32 , 0x30}, // ASCII value 3320
		{0x33 , 0x33 , 0x32 , 0x31}, // ASCII value 3321
		{0x33 , 0x33 , 0x32 , 0x32}, // ASCII value 3322
		{0x33 , 0x33 , 0x32 , 0x33}, // ASCII value 3323
		{0x33 , 0x33 , 0x32 , 0x34}, // ASCII value 3324
		{0x33 , 0x33 , 0x32 , 0x35}, // ASCII value 3325
		{0x33 , 0x33 , 0x32 , 0x36}, // ASCII value 3326
		{0x33 , 0x33 , 0x32 , 0x37}, // ASCII value 3327
		{0x33 , 0x33 , 0x32 , 0x38}, // ASCII value 3328
		{0x33 , 0x33 , 0x32 , 0x39}, // ASCII value 3329
		{0x33 , 0x33 , 0x33 , 0x30}, // ASCII value 3330
		{0x33 , 0x33 , 0x33 , 0x31}, // ASCII value 3331
		{0x33 , 0x33 , 0x33 , 0x32}, // ASCII value 3332
		{0x33 , 0x33 , 0x33 , 0x33}, // ASCII value 3333
		{0x33 , 0x33 , 0x33 , 0x34}, // ASCII value 3334
		{0x33 , 0x33 , 0x33 , 0x35}, // ASCII value 3335
		{0x33 , 0x33 , 0x33 , 0x36}, // ASCII value 3336
		{0x33 , 0x33 , 0x33 , 0x37}, // ASCII value 3337
		{0x33 , 0x33 , 0x33 , 0x38}, // ASCII value 3338
		{0x33 , 0x33 , 0x33 , 0x39}, // ASCII value 3339
		{0x33 , 0x33 , 0x34 , 0x30}, // ASCII value 3340
		{0x33 , 0x33 , 0x34 , 0x31}, // ASCII value 3341
		{0x33 , 0x33 , 0x34 , 0x32}, // ASCII value 3342
		{0x33 , 0x33 , 0x34 , 0x33}, // ASCII value 3343
		{0x33 , 0x33 , 0x34 , 0x34}, // ASCII value 3344
		{0x33 , 0x33 , 0x34 , 0x35}, // ASCII value 3345
		{0x33 , 0x33 , 0x34 , 0x36}, // ASCII value 3346
		{0x33 , 0x33 , 0x34 , 0x37}, // ASCII value 3347
		{0x33 , 0x33 , 0x34 , 0x38}, // ASCII value 3348
		{0x33 , 0x33 , 0x34 , 0x39}, // ASCII value 3349
		{0x33 , 0x33 , 0x35 , 0x30}, // ASCII value 3350
		{0x33 , 0x33 , 0x35 , 0x31}, // ASCII value 3351
		{0x33 , 0x33 , 0x35 , 0x32}, // ASCII value 3352
		{0x33 , 0x33 , 0x35 , 0x33}, // ASCII value 3353
		{0x33 , 0x33 , 0x35 , 0x34}, // ASCII value 3354
		{0x33 , 0x33 , 0x35 , 0x35}, // ASCII value 3355
		{0x33 , 0x33 , 0x35 , 0x36}, // ASCII value 3356
		{0x33 , 0x33 , 0x35 , 0x37}, // ASCII value 3357
		{0x33 , 0x33 , 0x35 , 0x38}, // ASCII value 3358
		{0x33 , 0x33 , 0x35 , 0x39}, // ASCII value 3359
		{0x33 , 0x33 , 0x36 , 0x30}, // ASCII value 3360
		{0x33 , 0x33 , 0x36 , 0x31}, // ASCII value 3361
		{0x33 , 0x33 , 0x36 , 0x32}, // ASCII value 3362
		{0x33 , 0x33 , 0x36 , 0x33}, // ASCII value 3363
		{0x33 , 0x33 , 0x36 , 0x34}, // ASCII value 3364
		{0x33 , 0x33 , 0x36 , 0x35}, // ASCII value 3365
		{0x33 , 0x33 , 0x36 , 0x36}, // ASCII value 3366
		{0x33 , 0x33 , 0x36 , 0x37}, // ASCII value 3367
		{0x33 , 0x33 , 0x36 , 0x38}, // ASCII value 3368
		{0x33 , 0x33 , 0x36 , 0x39}, // ASCII value 3369
		{0x33 , 0x33 , 0x37 , 0x30}, // ASCII value 3370
		{0x33 , 0x33 , 0x37 , 0x31}, // ASCII value 3371
		{0x33 , 0x33 , 0x37 , 0x32}, // ASCII value 3372
		{0x33 , 0x33 , 0x37 , 0x33}, // ASCII value 3373
		{0x33 , 0x33 , 0x37 , 0x34}, // ASCII value 3374
		{0x33 , 0x33 , 0x37 , 0x35}, // ASCII value 3375
		{0x33 , 0x33 , 0x37 , 0x36}, // ASCII value 3376
		{0x33 , 0x33 , 0x37 , 0x37}, // ASCII value 3377
		{0x33 , 0x33 , 0x37 , 0x38}, // ASCII value 3378
		{0x33 , 0x33 , 0x37 , 0x39}, // ASCII value 3379
		{0x33 , 0x34 , 0x30 , 0x30}, // ASCII value 3400
		{0x33 , 0x34 , 0x30 , 0x31}, // ASCII value 3401
		{0x33 , 0x34 , 0x30 , 0x32}, // ASCII value 3402
		{0x33 , 0x34 , 0x30 , 0x33}, // ASCII value 3403
		{0x33 , 0x34 , 0x30 , 0x34}, // ASCII value 3404
		{0x33 , 0x34 , 0x30 , 0x35}, // ASCII value 3405
		{0x33 , 0x34 , 0x30 , 0x36}, // ASCII value 3406
		{0x33 , 0x34 , 0x30 , 0x37}, // ASCII value 3407
		{0x33 , 0x34 , 0x30 , 0x38}, // ASCII value 3408
		{0x33 , 0x34 , 0x30 , 0x39}, // ASCII value 3409
		{0x33 , 0x34 , 0x31 , 0x30}, // ASCII value 3410
		{0x33 , 0x34 , 0x31 , 0x31}, // ASCII value 3411
		{0x33 , 0x34 , 0x31 , 0x32}, // ASCII value 3412
		{0x33 , 0x34 , 0x31 , 0x33}, // ASCII value 3413
		{0x33 , 0x34 , 0x31 , 0x34}, // ASCII value 3414
		{0x33 , 0x34 , 0x31 , 0x35}, // ASCII value 3415
		{0x33 , 0x34 , 0x31 , 0x36}, // ASCII value 3416
		{0x33 , 0x34 , 0x31 , 0x37}, // ASCII value 3417
		{0x33 , 0x34 , 0x31 , 0x38}, // ASCII value 3418
		{0x33 , 0x34 , 0x31 , 0x39}, // ASCII value 3419
		{0x33 , 0x34 , 0x32 , 0x30}, // ASCII value 3420
		{0x33 , 0x34 , 0x32 , 0x31}, // ASCII value 3421
		{0x33 , 0x34 , 0x32 , 0x32}, // ASCII value 3422
		{0x33 , 0x34 , 0x32 , 0x33}, // ASCII value 3423
		{0x33 , 0x34 , 0x32 , 0x34}, // ASCII value 3424
		{0x33 , 0x34 , 0x32 , 0x35}, // ASCII value 3425
		{0x33 , 0x34 , 0x32 , 0x36}, // ASCII value 3426
		{0x33 , 0x34 , 0x32 , 0x37}, // ASCII value 3427
		{0x33 , 0x34 , 0x32 , 0x38}, // ASCII value 3428
		{0x33 , 0x34 , 0x32 , 0x39}, // ASCII value 3429
		{0x33 , 0x34 , 0x33 , 0x30}, // ASCII value 3430
		{0x33 , 0x34 , 0x33 , 0x31}, // ASCII value 3431
		{0x33 , 0x34 , 0x33 , 0x32}, // ASCII value 3432
		{0x33 , 0x34 , 0x33 , 0x33}, // ASCII value 3433
		{0x33 , 0x34 , 0x33 , 0x34}, // ASCII value 3434
		{0x33 , 0x34 , 0x33 , 0x35}, // ASCII value 3435
		{0x33 , 0x34 , 0x33 , 0x36}, // ASCII value 3436
		{0x33 , 0x34 , 0x33 , 0x37}, // ASCII value 3437
		{0x33 , 0x34 , 0x33 , 0x38}, // ASCII value 3438
		{0x33 , 0x34 , 0x33 , 0x39}, // ASCII value 3439
		{0x33 , 0x34 , 0x34 , 0x30}, // ASCII value 3440
		{0x33 , 0x34 , 0x34 , 0x31}, // ASCII value 3441
		{0x33 , 0x34 , 0x34 , 0x32}, // ASCII value 3442
		{0x33 , 0x34 , 0x34 , 0x33}, // ASCII value 3443
		{0x33 , 0x34 , 0x34 , 0x34}, // ASCII value 3444
		{0x33 , 0x34 , 0x34 , 0x35}, // ASCII value 3445
		{0x33 , 0x34 , 0x34 , 0x36}, // ASCII value 3446
		{0x33 , 0x34 , 0x34 , 0x37}, // ASCII value 3447
		{0x33 , 0x34 , 0x34 , 0x38}, // ASCII value 3448
		{0x33 , 0x34 , 0x34 , 0x39}, // ASCII value 3449
		{0x33 , 0x34 , 0x35 , 0x30}, // ASCII value 3450
		{0x33 , 0x34 , 0x35 , 0x31}, // ASCII value 3451
		{0x33 , 0x34 , 0x35 , 0x32}, // ASCII value 3452
		{0x33 , 0x34 , 0x35 , 0x33}, // ASCII value 3453
		{0x33 , 0x34 , 0x35 , 0x34}, // ASCII value 3454
		{0x33 , 0x34 , 0x35 , 0x35}, // ASCII value 3455
		{0x33 , 0x34 , 0x35 , 0x36}, // ASCII value 3456
		{0x33 , 0x34 , 0x35 , 0x37}, // ASCII value 3457
		{0x33 , 0x34 , 0x35 , 0x38}, // ASCII value 3458
		{0x33 , 0x34 , 0x35 , 0x39}, // ASCII value 3459
		{0x33 , 0x34 , 0x36 , 0x30}, // ASCII value 3460
		{0x33 , 0x34 , 0x36 , 0x31}, // ASCII value 3461
		{0x33 , 0x34 , 0x36 , 0x32}, // ASCII value 3462
		{0x33 , 0x34 , 0x36 , 0x33}, // ASCII value 3463
		{0x33 , 0x34 , 0x36 , 0x34}, // ASCII value 3464
		{0x33 , 0x34 , 0x36 , 0x35}, // ASCII value 3465
		{0x33 , 0x34 , 0x36 , 0x36}, // ASCII value 3466
		{0x33 , 0x34 , 0x36 , 0x37}, // ASCII value 3467
		{0x33 , 0x34 , 0x36 , 0x38}, // ASCII value 3468
		{0x33 , 0x34 , 0x36 , 0x39}, // ASCII value 3469
		{0x33 , 0x34 , 0x37 , 0x30}, // ASCII value 3470
		{0x33 , 0x34 , 0x37 , 0x31}, // ASCII value 3471
		{0x33 , 0x34 , 0x37 , 0x32}, // ASCII value 3472
		{0x33 , 0x34 , 0x37 , 0x33}, // ASCII value 3473
		{0x33 , 0x34 , 0x37 , 0x34}, // ASCII value 3474
		{0x33 , 0x34 , 0x37 , 0x35}, // ASCII value 3475
		{0x33 , 0x34 , 0x37 , 0x36}, // ASCII value 3476
		{0x33 , 0x34 , 0x37 , 0x37}, // ASCII value 3477
		{0x33 , 0x34 , 0x37 , 0x38}, // ASCII value 3478
		{0x33 , 0x34 , 0x37 , 0x39}, // ASCII value 3479
		{0x33 , 0x34 , 0x38 , 0x30}, // ASCII value 3480
		{0x33 , 0x34 , 0x38 , 0x31}, // ASCII value 3481
		{0x33 , 0x34 , 0x38 , 0x32}, // ASCII value 3482
		{0x33 , 0x34 , 0x38 , 0x33}, // ASCII value 3483
		{0x33 , 0x34 , 0x38 , 0x34}, // ASCII value 3484
		{0x33 , 0x34 , 0x38 , 0x35}, // ASCII value 3485
		{0x33 , 0x34 , 0x38 , 0x36}, // ASCII value 3486
		{0x33 , 0x34 , 0x38 , 0x37}, // ASCII value 3487
		{0x33 , 0x34 , 0x38 , 0x38}, // ASCII value 3488
		{0x33 , 0x34 , 0x38 , 0x39}, // ASCII value 3489
		{0x33 , 0x34 , 0x39 , 0x30}, // ASCII value 3490
		{0x33 , 0x34 , 0x39 , 0x31}, // ASCII value 3491
		{0x33 , 0x34 , 0x39 , 0x32}, // ASCII value 3492
		{0x33 , 0x34 , 0x39 , 0x33}, // ASCII value 3493
		{0x33 , 0x34 , 0x39 , 0x34}, // ASCII value 3494
		{0x33 , 0x34 , 0x39 , 0x35}, // ASCII value 3495
		{0x33 , 0x34 , 0x39 , 0x36}, // ASCII value 3496
		{0x33 , 0x34 , 0x39 , 0x37}, // ASCII value 3497
		{0x33 , 0x34 , 0x39 , 0x38}, // ASCII value 3498
		{0x33 , 0x34 , 0x39 , 0x39}, // ASCII value 3499
		{0x33 , 0x35 , 0x30 , 0x30}, // ASCII value 3500
		{0x33 , 0x35 , 0x30 , 0x31}, // ASCII value 3501
		{0x33 , 0x35 , 0x30 , 0x32}, // ASCII value 3502
		{0x33 , 0x35 , 0x30 , 0x33}, // ASCII value 3503
		{0x33 , 0x35 , 0x30 , 0x34}, // ASCII value 3504
		{0x33 , 0x35 , 0x30 , 0x35}, // ASCII value 3505
		{0x33 , 0x35 , 0x30 , 0x36}, // ASCII value 3506
		{0x33 , 0x35 , 0x30 , 0x37}, // ASCII value 3507
		{0x33 , 0x35 , 0x30 , 0x38}, // ASCII value 3508
		{0x33 , 0x35 , 0x30 , 0x39}, // ASCII value 3509
		{0x33 , 0x35 , 0x31 , 0x30}, // ASCII value 3510
		{0x33 , 0x35 , 0x31 , 0x31}, // ASCII value 3511
		{0x33 , 0x35 , 0x31 , 0x32}, // ASCII value 3512
		{0x33 , 0x35 , 0x31 , 0x33}, // ASCII value 3513
		{0x33 , 0x35 , 0x31 , 0x34}, // ASCII value 3514
		{0x33 , 0x35 , 0x31 , 0x35}, // ASCII value 3515
		{0x33 , 0x35 , 0x31 , 0x36}, // ASCII value 3516
		{0x33 , 0x35 , 0x31 , 0x37}, // ASCII value 3517
		{0x33 , 0x35 , 0x31 , 0x38}, // ASCII value 3518
		{0x33 , 0x35 , 0x31 , 0x39}, // ASCII value 3519
		{0x33 , 0x35 , 0x32 , 0x30}, // ASCII value 3520
		{0x33 , 0x35 , 0x32 , 0x31}, // ASCII value 3521
		{0x33 , 0x35 , 0x32 , 0x32}, // ASCII value 3522
		{0x33 , 0x35 , 0x32 , 0x33}, // ASCII value 3523
		{0x33 , 0x35 , 0x32 , 0x34}, // ASCII value 3524
		{0x33 , 0x35 , 0x32 , 0x35}, // ASCII value 3525
		{0x33 , 0x35 , 0x32 , 0x36}, // ASCII value 3526
		{0x33 , 0x35 , 0x32 , 0x37}, // ASCII value 3527
		{0x33 , 0x35 , 0x32 , 0x38}, // ASCII value 3528
		{0x33 , 0x35 , 0x32 , 0x39}, // ASCII value 3529
		{0x33 , 0x35 , 0x33 , 0x30}, // ASCII value 3530
		{0x33 , 0x35 , 0x33 , 0x31}, // ASCII value 3531
		{0x33 , 0x35 , 0x33 , 0x32}, // ASCII value 3532
		{0x33 , 0x35 , 0x33 , 0x33}, // ASCII value 3533
		{0x33 , 0x35 , 0x33 , 0x34}, // ASCII value 3534
		{0x33 , 0x35 , 0x33 , 0x35}, // ASCII value 3535
		{0x33 , 0x35 , 0x33 , 0x36}, // ASCII value 3536
		{0x33 , 0x35 , 0x33 , 0x37}, // ASCII value 3537
		{0x33 , 0x35 , 0x33 , 0x38}, // ASCII value 3538
		{0x33 , 0x35 , 0x33 , 0x39}, // ASCII value 3539
		{0x33 , 0x35 , 0x34 , 0x30}, // ASCII value 3540
		{0x33 , 0x35 , 0x34 , 0x31}, // ASCII value 3541
		{0x33 , 0x35 , 0x34 , 0x32}, // ASCII value 3542
		{0x33 , 0x35 , 0x34 , 0x33}, // ASCII value 3543
		{0x33 , 0x35 , 0x34 , 0x34}, // ASCII value 3544
		{0x33 , 0x35 , 0x34 , 0x35}, // ASCII value 3545
		{0x33 , 0x35 , 0x34 , 0x36}, // ASCII value 3546
		{0x33 , 0x35 , 0x34 , 0x37}, // ASCII value 3547
		{0x33 , 0x35 , 0x34 , 0x38}, // ASCII value 3548
		{0x33 , 0x35 , 0x34 , 0x39}, // ASCII value 3549
		{0x33 , 0x35 , 0x35 , 0x30}, // ASCII value 3550
		{0x33 , 0x35 , 0x35 , 0x31}, // ASCII value 3551
		{0x33 , 0x35 , 0x35 , 0x32}, // ASCII value 3552
		{0x33 , 0x35 , 0x35 , 0x33}, // ASCII value 3553
		{0x33 , 0x35 , 0x35 , 0x34}, // ASCII value 3554
		{0x33 , 0x35 , 0x35 , 0x35}, // ASCII value 3555
		{0x33 , 0x35 , 0x35 , 0x36}, // ASCII value 3556
		{0x33 , 0x35 , 0x35 , 0x37}, // ASCII value 3557
		{0x33 , 0x35 , 0x35 , 0x38}, // ASCII value 3558
		{0x33 , 0x35 , 0x35 , 0x39}, // ASCII value 3559
		{0x33 , 0x35 , 0x36 , 0x30}, // ASCII value 3560
		{0x33 , 0x35 , 0x36 , 0x31}, // ASCII value 3561
		{0x33 , 0x35 , 0x36 , 0x32}, // ASCII value 3562
		{0x33 , 0x35 , 0x36 , 0x33}, // ASCII value 3563
		{0x33 , 0x35 , 0x36 , 0x34}, // ASCII value 3564
		{0x33 , 0x35 , 0x36 , 0x35}, // ASCII value 3565
		{0x33 , 0x35 , 0x36 , 0x36}, // ASCII value 3566
		{0x33 , 0x35 , 0x36 , 0x37}, // ASCII value 3567
		{0x33 , 0x35 , 0x36 , 0x38}, // ASCII value 3568
		{0x33 , 0x35 , 0x36 , 0x39}, // ASCII value 3569
		{0x33 , 0x35 , 0x37 , 0x30}, // ASCII value 3570
		{0x33 , 0x35 , 0x37 , 0x31}, // ASCII value 3571
		{0x33 , 0x35 , 0x37 , 0x32}, // ASCII value 3572
		{0x33 , 0x35 , 0x37 , 0x33}, // ASCII value 3573
		{0x33 , 0x35 , 0x37 , 0x34}, // ASCII value 3574
		{0x33 , 0x35 , 0x37 , 0x35}, // ASCII value 3575
		{0x33 , 0x35 , 0x37 , 0x36}, // ASCII value 3576
		{0x33 , 0x35 , 0x37 , 0x37}, // ASCII value 3577
		{0x33 , 0x35 , 0x37 , 0x38}, // ASCII value 3578
		{0x33 , 0x35 , 0x37 , 0x39}, // ASCII value 3579
		{0x33 , 0x36 , 0x30 , 0x30}, // ASCII value 3600
		{0x33 , 0x36 , 0x30 , 0x31}, // ASCII value 3601
		{0x33 , 0x36 , 0x30 , 0x32}, // ASCII value 3602
		{0x33 , 0x36 , 0x30 , 0x33}, // ASCII value 3603
		{0x33 , 0x36 , 0x30 , 0x34}, // ASCII value 3604
		{0x33 , 0x36 , 0x30 , 0x35}, // ASCII value 3605
		{0x33 , 0x36 , 0x30 , 0x36}, // ASCII value 3606
		{0x33 , 0x36 , 0x30 , 0x37}, // ASCII value 3607
		{0x33 , 0x36 , 0x30 , 0x38}, // ASCII value 3608
		{0x33 , 0x36 , 0x30 , 0x39}, // ASCII value 3609
		{0x33 , 0x36 , 0x31 , 0x30}, // ASCII value 3610
		{0x33 , 0x36 , 0x31 , 0x31}, // ASCII value 3611
		{0x33 , 0x36 , 0x31 , 0x32}, // ASCII value 3612
		{0x33 , 0x36 , 0x31 , 0x33}, // ASCII value 3613
		{0x33 , 0x36 , 0x31 , 0x34}, // ASCII value 3614
		{0x33 , 0x36 , 0x31 , 0x35}, // ASCII value 3615
		{0x33 , 0x36 , 0x31 , 0x36}, // ASCII value 3616
		{0x33 , 0x36 , 0x31 , 0x37}, // ASCII value 3617
		{0x33 , 0x36 , 0x31 , 0x38}, // ASCII value 3618
		{0x33 , 0x36 , 0x31 , 0x39}, // ASCII value 3619
		{0x33 , 0x36 , 0x32 , 0x30}, // ASCII value 3620
		{0x33 , 0x36 , 0x32 , 0x31}, // ASCII value 3621
		{0x33 , 0x36 , 0x32 , 0x32}, // ASCII value 3622
		{0x33 , 0x36 , 0x32 , 0x33}, // ASCII value 3623
		{0x33 , 0x36 , 0x32 , 0x34}, // ASCII value 3624
		{0x33 , 0x36 , 0x32 , 0x35}, // ASCII value 3625
		{0x33 , 0x36 , 0x32 , 0x36}, // ASCII value 3626
		{0x33 , 0x36 , 0x32 , 0x37}, // ASCII value 3627
		{0x33 , 0x36 , 0x32 , 0x38}, // ASCII value 3628
		{0x33 , 0x36 , 0x32 , 0x39}, // ASCII value 3629
		{0x33 , 0x36 , 0x33 , 0x30}, // ASCII value 3630
		{0x33 , 0x36 , 0x33 , 0x31}, // ASCII value 3631
		{0x33 , 0x36 , 0x33 , 0x32}, // ASCII value 3632
		{0x33 , 0x36 , 0x33 , 0x33}, // ASCII value 3633
		{0x33 , 0x36 , 0x33 , 0x34}, // ASCII value 3634
		{0x33 , 0x36 , 0x33 , 0x35}, // ASCII value 3635
		{0x33 , 0x36 , 0x33 , 0x36}, // ASCII value 3636
		{0x33 , 0x36 , 0x33 , 0x37}, // ASCII value 3637
		{0x33 , 0x36 , 0x33 , 0x38}, // ASCII value 3638
		{0x33 , 0x36 , 0x33 , 0x39}, // ASCII value 3639
		{0x33 , 0x36 , 0x34 , 0x30}, // ASCII value 3640
		{0x33 , 0x36 , 0x34 , 0x31}, // ASCII value 3641
		{0x33 , 0x36 , 0x34 , 0x32}, // ASCII value 3642
		{0x33 , 0x36 , 0x34 , 0x33}, // ASCII value 3643
		{0x33 , 0x36 , 0x34 , 0x34}, // ASCII value 3644
		{0x33 , 0x36 , 0x34 , 0x35}, // ASCII value 3645
		{0x33 , 0x36 , 0x34 , 0x36}, // ASCII value 3646
		{0x33 , 0x36 , 0x34 , 0x37}, // ASCII value 3647
		{0x33 , 0x36 , 0x34 , 0x38}, // ASCII value 3648
		{0x33 , 0x36 , 0x34 , 0x39}, // ASCII value 3649
		{0x33 , 0x36 , 0x35 , 0x30}, // ASCII value 3650
		{0x33 , 0x36 , 0x35 , 0x31}, // ASCII value 3651
		{0x33 , 0x36 , 0x35 , 0x32}, // ASCII value 3652
		{0x33 , 0x36 , 0x35 , 0x33}, // ASCII value 3653
		{0x33 , 0x36 , 0x35 , 0x34}, // ASCII value 3654
		{0x33 , 0x36 , 0x35 , 0x35}, // ASCII value 3655
		{0x33 , 0x36 , 0x35 , 0x36}, // ASCII value 3656
		{0x33 , 0x36 , 0x35 , 0x37}, // ASCII value 3657
		{0x33 , 0x36 , 0x35 , 0x38}, // ASCII value 3658
		{0x33 , 0x36 , 0x35 , 0x39}, // ASCII value 3659
		{0x33 , 0x36 , 0x36 , 0x30}, // ASCII value 3660
		{0x33 , 0x36 , 0x36 , 0x31}, // ASCII value 3661
		{0x33 , 0x36 , 0x36 , 0x32}, // ASCII value 3662
		{0x33 , 0x36 , 0x36 , 0x33}, // ASCII value 3663
		{0x33 , 0x36 , 0x36 , 0x34}, // ASCII value 3664
		{0x33 , 0x36 , 0x36 , 0x35}, // ASCII value 3665
		{0x33 , 0x36 , 0x36 , 0x36}, // ASCII value 3666
		{0x33 , 0x36 , 0x36 , 0x37}, // ASCII value 3667
		{0x33 , 0x36 , 0x36 , 0x38}, // ASCII value 3668
		{0x33 , 0x36 , 0x36 , 0x39}, // ASCII value 3669
		{0x33 , 0x36 , 0x37 , 0x30}, // ASCII value 3670
		{0x33 , 0x36 , 0x37 , 0x31}, // ASCII value 3671
		{0x33 , 0x36 , 0x37 , 0x32}, // ASCII value 3672
		{0x33 , 0x36 , 0x37 , 0x33}, // ASCII value 3673
		{0x33 , 0x36 , 0x37 , 0x34}, // ASCII value 3674
		{0x33 , 0x36 , 0x37 , 0x35}, // ASCII value 3675
		{0x33 , 0x36 , 0x37 , 0x36}, // ASCII value 3676
		{0x33 , 0x36 , 0x37 , 0x37}, // ASCII value 3677
		{0x33 , 0x36 , 0x37 , 0x38}, // ASCII value 3678
		{0x33 , 0x36 , 0x37 , 0x39}, // ASCII value 3679
		{0x33 , 0x36 , 0x38 , 0x30}, // ASCII value 3680
		{0x33 , 0x36 , 0x38 , 0x31}, // ASCII value 3681
		{0x33 , 0x36 , 0x38 , 0x32}, // ASCII value 3682
		{0x33 , 0x36 , 0x38 , 0x33}, // ASCII value 3683
		{0x33 , 0x36 , 0x38 , 0x34}, // ASCII value 3684
		{0x33 , 0x36 , 0x38 , 0x35}, // ASCII value 3685
		{0x33 , 0x36 , 0x38 , 0x36}, // ASCII value 3686
		{0x33 , 0x36 , 0x38 , 0x37}, // ASCII value 3687
		{0x33 , 0x36 , 0x38 , 0x38}, // ASCII value 3688
		{0x33 , 0x36 , 0x38 , 0x39}, // ASCII value 3689
		{0x33 , 0x36 , 0x39 , 0x30}, // ASCII value 3690
		{0x33 , 0x36 , 0x39 , 0x31}, // ASCII value 3691
		{0x33 , 0x36 , 0x39 , 0x32}, // ASCII value 3692
		{0x33 , 0x36 , 0x39 , 0x33}, // ASCII value 3693
		{0x33 , 0x36 , 0x39 , 0x34}, // ASCII value 3694
		{0x33 , 0x36 , 0x39 , 0x35}, // ASCII value 3695
		{0x33 , 0x36 , 0x39 , 0x36}, // ASCII value 3696
		{0x33 , 0x36 , 0x39 , 0x37}, // ASCII value 3697
		{0x33 , 0x36 , 0x39 , 0x38}, // ASCII value 3698
		{0x33 , 0x36 , 0x39 , 0x39}, // ASCII value 3699
		{0x33 , 0x39 , 0x30 , 0x30}, // ASCII value 3900
		{0x33 , 0x39 , 0x30 , 0x31}, // ASCII value 3901
		{0x33 , 0x39 , 0x30 , 0x32}, // ASCII value 3902
		{0x33 , 0x39 , 0x30 , 0x33}, // ASCII value 3903
		{0x33 , 0x39 , 0x30 , 0x34}, // ASCII value 3904
		{0x33 , 0x39 , 0x30 , 0x35}, // ASCII value 3905
		{0x33 , 0x39 , 0x30 , 0x36}, // ASCII value 3906
		{0x33 , 0x39 , 0x30 , 0x37}, // ASCII value 3907
		{0x33 , 0x39 , 0x30 , 0x38}, // ASCII value 3908
		{0x33 , 0x39 , 0x30 , 0x39}, // ASCII value 3909
		{0x33 , 0x39 , 0x31 , 0x30}, // ASCII value 3910
		{0x33 , 0x39 , 0x31 , 0x31}, // ASCII value 3911
		{0x33 , 0x39 , 0x31 , 0x32}, // ASCII value 3912
		{0x33 , 0x39 , 0x31 , 0x33}, // ASCII value 3913
		{0x33 , 0x39 , 0x31 , 0x34}, // ASCII value 3914
		{0x33 , 0x39 , 0x31 , 0x35}, // ASCII value 3915
		{0x33 , 0x39 , 0x31 , 0x36}, // ASCII value 3916
		{0x33 , 0x39 , 0x31 , 0x37}, // ASCII value 3917
		{0x33 , 0x39 , 0x31 , 0x38}, // ASCII value 3918
		{0x33 , 0x39 , 0x31 , 0x39}, // ASCII value 3919
		{0x33 , 0x39 , 0x32 , 0x30}, // ASCII value 3920
		{0x33 , 0x39 , 0x32 , 0x31}, // ASCII value 3921
		{0x33 , 0x39 , 0x32 , 0x32}, // ASCII value 3922
		{0x33 , 0x39 , 0x32 , 0x33}, // ASCII value 3923
		{0x33 , 0x39 , 0x32 , 0x34}, // ASCII value 3924
		{0x33 , 0x39 , 0x32 , 0x35}, // ASCII value 3925
		{0x33 , 0x39 , 0x32 , 0x36}, // ASCII value 3926
		{0x33 , 0x39 , 0x32 , 0x37}, // ASCII value 3927
		{0x33 , 0x39 , 0x32 , 0x38}, // ASCII value 3928
		{0x33 , 0x39 , 0x32 , 0x39}, // ASCII value 3929
		{0x33 , 0x39 , 0x33 , 0x30}, // ASCII value 3930
		{0x33 , 0x39 , 0x33 , 0x31}, // ASCII value 3931
		{0x33 , 0x39 , 0x33 , 0x32}, // ASCII value 3932
		{0x33 , 0x39 , 0x33 , 0x33}, // ASCII value 3933
		{0x33 , 0x39 , 0x33 , 0x34}, // ASCII value 3934
		{0x33 , 0x39 , 0x33 , 0x35}, // ASCII value 3935
		{0x33 , 0x39 , 0x33 , 0x36}, // ASCII value 3936
		{0x33 , 0x39 , 0x33 , 0x37}, // ASCII value 3937
		{0x33 , 0x39 , 0x33 , 0x38}, // ASCII value 3938
		{0x33 , 0x39 , 0x33 , 0x39}, // ASCII value 3939
		{0x37 , 0x30 , 0x30 , 0x31}, // ASCII value 7001
		{0x37 , 0x30 , 0x30 , 0x32}, // ASCII value 7002
		{0x37 , 0x30 , 0x30 , 0x33}, // ASCII value 7003
		{0x37 , 0x30 , 0x33 , 0x73}, // ASCII value 703s
		{0x38 , 0x30 , 0x30 , 0x31}, // ASCII value 8001
		{0x38 , 0x30 , 0x30 , 0x32}, // ASCII value 8002
		{0x38 , 0x30 , 0x30 , 0x33}, // ASCII value 8003
		{0x38 , 0x30 , 0x30 , 0x34}, // ASCII value 8004
		{0x38 , 0x30 , 0x30 , 0x35}, // ASCII value 8005
		{0x38 , 0x30 , 0x30 , 0x36}, // ASCII value 8006
		{0x38 , 0x30 , 0x30 , 0x37}, // ASCII value 8007
		{0x38 , 0x30 , 0x30 , 0x38}, // ASCII value 8008
		{0x38 , 0x30 , 0x31 , 0x38}, // ASCII value 8018
		{0x38 , 0x30 , 0x32 , 0x30}, // ASCII value 8020
		{0x38 , 0x31 , 0x30 , 0x30}, // ASCII value 8100
		{0x38 , 0x31 , 0x30 , 0x31}, // ASCII value 8101
		{0x38 , 0x31 , 0x30 , 0x32}, // ASCII value 8102
		{0x38 , 0x31 , 0x31 , 0x30}  // ASCII value 8110
	};

	private static final int[] STARTSEPARATORTABLELENGTH4 = {
		10, // 3100 max length is 10
		10, // 3101 max length is 10
		10, // 3102 max length is 10
		10, // 3103 max length is 10
		10, // 3104 max length is 10
		10, // 3105 max length is 10
		10, // 3106 max length is 10
		10, // 3107 max length is 10
		10, // 3108 max length is 10
		10, // 3109 max length is 10
		10, // 3110 max length is 10
		10, // 3111 max length is 10
		10, // 3112 max length is 10
		10, // 3113 max length is 10
		10, // 3114 max length is 10
		10, // 3115 max length is 10
		10, // 3116 max length is 10
		10, // 3117 max length is 10
		10, // 3118 max length is 10
		10, // 3119 max length is 10
		10, // 3120 max length is 10
		10, // 3121 max length is 10
		10, // 3122 max length is 10
		10, // 3123 max length is 10
		10, // 3124 max length is 10
		10, // 3125 max length is 10
		10, // 3126 max length is 10
		10, // 3127 max length is 10
		10, // 3128 max length is 10
		10, // 3129 max length is 10
		10, // 3130 max length is 10
		10, // 3131 max length is 10
		10, // 3132 max length is 10
		10, // 3133 max length is 10
		10, // 3134 max length is 10
		10, // 3135 max length is 10
		10, // 3136 max length is 10
		10, // 3137 max length is 10
		10, // 3138 max length is 10
		10, // 3139 max length is 10
		10, // 3140 max length is 10
		10, // 3141 max length is 10
		10, // 3142 max length is 10
		10, // 3143 max length is 10
		10, // 3144 max length is 10
		10, // 3145 max length is 10
		10, // 3146 max length is 10
		10, // 3147 max length is 10
		10, // 3148 max length is 10
		10, // 3149 max length is 10
		10, // 3150 max length is 10
		10, // 3151 max length is 10
		10, // 3152 max length is 10
		10, // 3153 max length is 10
		10, // 3154 max length is 10
		10, // 3155 max length is 10
		10, // 3156 max length is 10
		10, // 3157 max length is 10
		10, // 3158 max length is 10
		10, // 3159 max length is 10
		10, // 3160 max length is 10
		10, // 3161 max length is 10
		10, // 3162 max length is 10
		10, // 3163 max length is 10
		10, // 3164 max length is 10
		10, // 3165 max length is 10
		10, // 3166 max length is 10
		10, // 3167 max length is 10
		10, // 3168 max length is 10
		10, // 3169 max length is 10
		10, // 3200 max length is 10
		10, // 3201 max length is 10
		10, // 3202 max length is 10
		10, // 3203 max length is 10
		10, // 3204 max length is 10
		10, // 3205 max length is 10
		10, // 3206 max length is 10
		10, // 3207 max length is 10
		10, // 3208 max length is 10
		10, // 3209 max length is 10
		10, // 3210 max length is 10
		10, // 3211 max length is 10
		10, // 3212 max length is 10
		10, // 3213 max length is 10
		10, // 3214 max length is 10
		10, // 3215 max length is 10
		10, // 3216 max length is 10
		10, // 3217 max length is 10
		10, // 3218 max length is 10
		10, // 3219 max length is 10
		10, // 3220 max length is 10
		10, // 3221 max length is 10
		10, // 3222 max length is 10
		10, // 3223 max length is 10
		10, // 3224 max length is 10
		10, // 3225 max length is 10
		10, // 3226 max length is 10
		10, // 3227 max length is 10
		10, // 3228 max length is 10
		10, // 3229 max length is 10
		10, // 3230 max length is 10
		10, // 3231 max length is 10
		10, // 3232 max length is 10
		10, // 3233 max length is 10
		10, // 3234 max length is 10
		10, // 3235 max length is 10
		10, // 3236 max length is 10
		10, // 3237 max length is 10
		10, // 3238 max length is 10
		10, // 3239 max length is 10
		10, // 3240 max length is 10
		10, // 3241 max length is 10
		10, // 3242 max length is 10
		10, // 3243 max length is 10
		10, // 3244 max length is 10
		10, // 3245 max length is 10
		10, // 3246 max length is 10
		10, // 3247 max length is 10
		10, // 3248 max length is 10
		10, // 3249 max length is 10
		10, // 3250 max length is 10
		10, // 3251 max length is 10
		10, // 3252 max length is 10
		10, // 3253 max length is 10
		10, // 3254 max length is 10
		10, // 3255 max length is 10
		10, // 3256 max length is 10
		10, // 3257 max length is 10
		10, // 3258 max length is 10
		10, // 3259 max length is 10
		10, // 3260 max length is 10
		10, // 3261 max length is 10
		10, // 3262 max length is 10
		10, // 3263 max length is 10
		10, // 3264 max length is 10
		10, // 3265 max length is 10
		10, // 3266 max length is 10
		10, // 3267 max length is 10
		10, // 3268 max length is 10
		10, // 3269 max length is 10
		10, // 3270 max length is 10
		10, // 3271 max length is 10
		10, // 3272 max length is 10
		10, // 3273 max length is 10
		10, // 3274 max length is 10
		10, // 3275 max length is 10
		10, // 3276 max length is 10
		10, // 3277 max length is 10
		10, // 3278 max length is 10
		10, // 3279 max length is 10
		10, // 3280 max length is 10
		10, // 3281 max length is 10
		10, // 3282 max length is 10
		10, // 3283 max length is 10
		10, // 3284 max length is 10
		10, // 3285 max length is 10
		10, // 3286 max length is 10
		10, // 3287 max length is 10
		10, // 3288 max length is 10
		10, // 3289 max length is 10
		10, // 3290 max length is 10
		10, // 3291 max length is 10
		10, // 3292 max length is 10
		10, // 3293 max length is 10
		10, // 3294 max length is 10
		10, // 3295 max length is 10
		10, // 3296 max length is 10
		10, // 3297 max length is 10
		10, // 3298 max length is 10
		10, // 3299 max length is 10
		10, // 3300 max length is 10
		10, // 3301 max length is 10
		10, // 3302 max length is 10
		10, // 3303 max length is 10
		10, // 3304 max length is 10
		10, // 3305 max length is 10
		10, // 3306 max length is 10
		10, // 3307 max length is 10
		10, // 3308 max length is 10
		10, // 3309 max length is 10
		10, // 3310 max length is 10
		10, // 3311 max length is 10
		10, // 3312 max length is 10
		10, // 3313 max length is 10
		10, // 3314 max length is 10
		10, // 3315 max length is 10
		10, // 3316 max length is 10
		10, // 3317 max length is 10
		10, // 3318 max length is 10
		10, // 3319 max length is 10
		10, // 3320 max length is 10
		10, // 3321 max length is 10
		10, // 3322 max length is 10
		10, // 3323 max length is 10
		10, // 3324 max length is 10
		10, // 3325 max length is 10
		10, // 3326 max length is 10
		10, // 3327 max length is 10
		10, // 3328 max length is 10
		10, // 3329 max length is 10
		10, // 3330 max length is 10
		10, // 3331 max length is 10
		10, // 3332 max length is 10
		10, // 3333 max length is 10
		10, // 3334 max length is 10
		10, // 3335 max length is 10
		10, // 3336 max length is 10
		10, // 3337 max length is 10
		10, // 3338 max length is 10
		10, // 3339 max length is 10
		10, // 3340 max length is 10
		10, // 3341 max length is 10
		10, // 3342 max length is 10
		10, // 3343 max length is 10
		10, // 3344 max length is 10
		10, // 3345 max length is 10
		10, // 3346 max length is 10
		10, // 3347 max length is 10
		10, // 3348 max length is 10
		10, // 3349 max length is 10
		10, // 3350 max length is 10
		10, // 3351 max length is 10
		10, // 3352 max length is 10
		10, // 3353 max length is 10
		10, // 3354 max length is 10
		10, // 3355 max length is 10
		10, // 3356 max length is 10
		10, // 3357 max length is 10
		10, // 3358 max length is 10
		10, // 3359 max length is 10
		10, // 3360 max length is 10
		10, // 3361 max length is 10
		10, // 3362 max length is 10
		10, // 3363 max length is 10
		10, // 3364 max length is 10
		10, // 3365 max length is 10
		10, // 3366 max length is 10
		10, // 3367 max length is 10
		10, // 3368 max length is 10
		10, // 3369 max length is 10
		10, // 3370 max length is 10
		10, // 3371 max length is 10
		10, // 3372 max length is 10
		10, // 3373 max length is 10
		10, // 3374 max length is 10
		10, // 3375 max length is 10
		10, // 3376 max length is 10
		10, // 3377 max length is 10
		10, // 3378 max length is 10
		10, // 3379 max length is 10
		10, // 3400 max length is 10
		10, // 3401 max length is 10
		10, // 3402 max length is 10
		10, // 3403 max length is 10
		10, // 3404 max length is 10
		10, // 3405 max length is 10
		10, // 3406 max length is 10
		10, // 3407 max length is 10
		10, // 3408 max length is 10
		10, // 3409 max length is 10
		10, // 3410 max length is 10
		10, // 3411 max length is 10
		10, // 3412 max length is 10
		10, // 3413 max length is 10
		10, // 3414 max length is 10
		10, // 3415 max length is 10
		10, // 3416 max length is 10
		10, // 3417 max length is 10
		10, // 3418 max length is 10
		10, // 3419 max length is 10
		10, // 3420 max length is 10
		10, // 3421 max length is 10
		10, // 3422 max length is 10
		10, // 3423 max length is 10
		10, // 3424 max length is 10
		10, // 3425 max length is 10
		10, // 3426 max length is 10
		10, // 3427 max length is 10
		10, // 3428 max length is 10
		10, // 3429 max length is 10
		10, // 3430 max length is 10
		10, // 3431 max length is 10
		10, // 3432 max length is 10
		10, // 3433 max length is 10
		10, // 3434 max length is 10
		10, // 3435 max length is 10
		10, // 3436 max length is 10
		10, // 3437 max length is 10
		10, // 3438 max length is 10
		10, // 3439 max length is 10
		10, // 3440 max length is 10
		10, // 3441 max length is 10
		10, // 3442 max length is 10
		10, // 3443 max length is 10
		10, // 3444 max length is 10
		10, // 3445 max length is 10
		10, // 3446 max length is 10
		10, // 3447 max length is 10
		10, // 3448 max length is 10
		10, // 3449 max length is 10
		10, // 3450 max length is 10
		10, // 3451 max length is 10
		10, // 3452 max length is 10
		10, // 3453 max length is 10
		10, // 3454 max length is 10
		10, // 3455 max length is 10
		10, // 3456 max length is 10
		10, // 3457 max length is 10
		10, // 3458 max length is 10
		10, // 3459 max length is 10
		10, // 3460 max length is 10
		10, // 3461 max length is 10
		10, // 3462 max length is 10
		10, // 3463 max length is 10
		10, // 3464 max length is 10
		10, // 3465 max length is 10
		10, // 3466 max length is 10
		10, // 3467 max length is 10
		10, // 3468 max length is 10
		10, // 3469 max length is 10
		10, // 3470 max length is 10
		10, // 3471 max length is 10
		10, // 3472 max length is 10
		10, // 3473 max length is 10
		10, // 3474 max length is 10
		10, // 3475 max length is 10
		10, // 3476 max length is 10
		10, // 3477 max length is 10
		10, // 3478 max length is 10
		10, // 3479 max length is 10
		10, // 3480 max length is 10
		10, // 3481 max length is 10
		10, // 3482 max length is 10
		10, // 3483 max length is 10
		10, // 3484 max length is 10
		10, // 3485 max length is 10
		10, // 3486 max length is 10
		10, // 3487 max length is 10
		10, // 3488 max length is 10
		10, // 3489 max length is 10
		10, // 3490 max length is 10
		10, // 3491 max length is 10
		10, // 3492 max length is 10
		10, // 3493 max length is 10
		10, // 3494 max length is 10
		10, // 3495 max length is 10
		10, // 3496 max length is 10
		10, // 3497 max length is 10
		10, // 3498 max length is 10
		10, // 3499 max length is 10
		10, // 3500 max length is 10
		10, // 3501 max length is 10
		10, // 3502 max length is 10
		10, // 3503 max length is 10
		10, // 3504 max length is 10
		10, // 3505 max length is 10
		10, // 3506 max length is 10
		10, // 3507 max length is 10
		10, // 3508 max length is 10
		10, // 3509 max length is 10
		10, // 3510 max length is 10
		10, // 3511 max length is 10
		10, // 3512 max length is 10
		10, // 3513 max length is 10
		10, // 3514 max length is 10
		10, // 3515 max length is 10
		10, // 3516 max length is 10
		10, // 3517 max length is 10
		10, // 3518 max length is 10
		10, // 3519 max length is 10
		10, // 3520 max length is 10
		10, // 3521 max length is 10
		10, // 3522 max length is 10
		10, // 3523 max length is 10
		10, // 3524 max length is 10
		10, // 3525 max length is 10
		10, // 3526 max length is 10
		10, // 3527 max length is 10
		10, // 3528 max length is 10
		10, // 3529 max length is 10
		10, // 3530 max length is 10
		10, // 3531 max length is 10
		10, // 3532 max length is 10
		10, // 3533 max length is 10
		10, // 3534 max length is 10
		10, // 3535 max length is 10
		10, // 3536 max length is 10
		10, // 3537 max length is 10
		10, // 3538 max length is 10
		10, // 3539 max length is 10
		10, // 3540 max length is 10
		10, // 3541 max length is 10
		10, // 3542 max length is 10
		10, // 3543 max length is 10
		10, // 3544 max length is 10
		10, // 3545 max length is 10
		10, // 3546 max length is 10
		10, // 3547 max length is 10
		10, // 3548 max length is 10
		10, // 3549 max length is 10
		10, // 3550 max length is 10
		10, // 3551 max length is 10
		10, // 3552 max length is 10
		10, // 3553 max length is 10
		10, // 3554 max length is 10
		10, // 3555 max length is 10
		10, // 3556 max length is 10
		10, // 3557 max length is 10
		10, // 3558 max length is 10
		10, // 3559 max length is 10
		10, // 3560 max length is 10
		10, // 3561 max length is 10
		10, // 3562 max length is 10
		10, // 3563 max length is 10
		10, // 3564 max length is 10
		10, // 3565 max length is 10
		10, // 3566 max length is 10
		10, // 3567 max length is 10
		10, // 3568 max length is 10
		10, // 3569 max length is 10
		10, // 3570 max length is 10
		10, // 3571 max length is 10
		10, // 3572 max length is 10
		10, // 3573 max length is 10
		10, // 3574 max length is 10
		10, // 3575 max length is 10
		10, // 3576 max length is 10
		10, // 3577 max length is 10
		10, // 3578 max length is 10
		10, // 3579 max length is 10
		10, // 3600 max length is 10
		10, // 3601 max length is 10
		10, // 3602 max length is 10
		10, // 3603 max length is 10
		10, // 3604 max length is 10
		10, // 3605 max length is 10
		10, // 3606 max length is 10
		10, // 3607 max length is 10
		10, // 3608 max length is 10
		10, // 3609 max length is 10
		10, // 3610 max length is 10
		10, // 3611 max length is 10
		10, // 3612 max length is 10
		10, // 3613 max length is 10
		10, // 3614 max length is 10
		10, // 3615 max length is 10
		10, // 3616 max length is 10
		10, // 3617 max length is 10
		10, // 3618 max length is 10
		10, // 3619 max length is 10
		10, // 3620 max length is 10
		10, // 3621 max length is 10
		10, // 3622 max length is 10
		10, // 3623 max length is 10
		10, // 3624 max length is 10
		10, // 3625 max length is 10
		10, // 3626 max length is 10
		10, // 3627 max length is 10
		10, // 3628 max length is 10
		10, // 3629 max length is 10
		10, // 3630 max length is 10
		10, // 3631 max length is 10
		10, // 3632 max length is 10
		10, // 3633 max length is 10
		10, // 3634 max length is 10
		10, // 3635 max length is 10
		10, // 3636 max length is 10
		10, // 3637 max length is 10
		10, // 3638 max length is 10
		10, // 3639 max length is 10
		10, // 3640 max length is 10
		10, // 3641 max length is 10
		10, // 3642 max length is 10
		10, // 3643 max length is 10
		10, // 3644 max length is 10
		10, // 3645 max length is 10
		10, // 3646 max length is 10
		10, // 3647 max length is 10
		10, // 3648 max length is 10
		10, // 3649 max length is 10
		10, // 3650 max length is 10
		10, // 3651 max length is 10
		10, // 3652 max length is 10
		10, // 3653 max length is 10
		10, // 3654 max length is 10
		10, // 3655 max length is 10
		10, // 3656 max length is 10
		10, // 3657 max length is 10
		10, // 3658 max length is 10
		10, // 3659 max length is 10
		10, // 3660 max length is 10
		10, // 3661 max length is 10
		10, // 3662 max length is 10
		10, // 3663 max length is 10
		10, // 3664 max length is 10
		10, // 3665 max length is 10
		10, // 3666 max length is 10
		10, // 3667 max length is 10
		10, // 3668 max length is 10
		10, // 3669 max length is 10
		10, // 3670 max length is 10
		10, // 3671 max length is 10
		10, // 3672 max length is 10
		10, // 3673 max length is 10
		10, // 3674 max length is 10
		10, // 3675 max length is 10
		10, // 3676 max length is 10
		10, // 3677 max length is 10
		10, // 3678 max length is 10
		10, // 3679 max length is 10
		10, // 3680 max length is 10
		10, // 3681 max length is 10
		10, // 3682 max length is 10
		10, // 3683 max length is 10
		10, // 3684 max length is 10
		10, // 3685 max length is 10
		10, // 3686 max length is 10
		10, // 3687 max length is 10
		10, // 3688 max length is 10
		10, // 3689 max length is 10
		10, // 3690 max length is 10
		10, // 3691 max length is 10
		10, // 3692 max length is 10
		10, // 3693 max length is 10
		10, // 3694 max length is 10
		10, // 3695 max length is 10
		10, // 3696 max length is 10
		10, // 3697 max length is 10
		10, // 3698 max length is 10
		10, // 3699 max length is 10
		19, // 3900 max length is 19
		19, // 3901 max length is 19
		19, // 3902 max length is 19
		19, // 3903 max length is 19
		19, // 3904 max length is 19
		19, // 3905 max length is 19
		19, // 3906 max length is 19
		19, // 3907 max length is 19
		19, // 3908 max length is 19
		19, // 3909 max length is 19
		22, // 3910 max length is 22
		22, // 3911 max length is 22
		22, // 3912 max length is 22
		22, // 3913 max length is 22
		22, // 3914 max length is 22
		22, // 3915 max length is 22
		22, // 3916 max length is 22
		22, // 3917 max length is 22
		22, // 3918 max length is 22
		22, // 3919 max length is 22
		19, // 3920 max length is 19
		19, // 3921 max length is 19
		19, // 3922 max length is 19
		19, // 3923 max length is 19
		19, // 3924 max length is 19
		19, // 3925 max length is 19
		19, // 3926 max length is 19
		19, // 3927 max length is 19
		19, // 3928 max length is 19
		19, // 3929 max length is 19
		22, // 3930 max length is 22
		22, // 3931 max length is 22
		22, // 3932 max length is 22
		22, // 3933 max length is 22
		22, // 3934 max length is 22
		22, // 3935 max length is 22
		22, // 3936 max length is 22
		22, // 3937 max length is 22
		22, // 3938 max length is 22
		22, // 3939 max length is 22
		17, // 7001 max length is 17
		34, // 7002 max length is 34
		14, // 7003 max length is 14
		34, // 703s max length is 34
		18, // 8001 max length is 18
		24, // 8002 max length is 24
		34, // 8003 max length is 34
		34, // 8004 max length is 34
		10, // 8005 max length is 10
		22, // 8006 max length is 22
		34, // 8007 max length is 34
		16, // 8008 max length is 16
		22, // 8018 max length is 22
		29, // 8020 max length is 29
		10, // 8100 max length is 10
		14, // 8101 max length is 14
		6,  // 8102 max length is 6
		34  // 8110 max length is 34
	};

	// urovo add shenpidong begin 2019-11-28
	public static final int compositeIndexCode(byte codeID) {
	    if(SEPARATORCODE[SEPARATORCODE.length-1] == codeID) {
		return 8; // UPC-E Composite max length is 8
	    } else if(SEPARATORCODE[SEPARATORCODE.length-2] == codeID) {
		return 12; // UPC-A Composite max length is 12
	    } else if(SEPARATORCODE[SEPARATORCODE.length-3] == codeID) {
		return 8; // EAN-8 Composite max length is 8
	    } else if(SEPARATORCODE[SEPARATORCODE.length-4] == codeID) {
		return 13; // EAN-13 Composite max length is 13
	    }
	    /*
	    int startComposite = SEPARATORCODE.length-1;
	    int endComposite = SEPARATORCODE.length - 1 - 4; // UPC-E Composite / UPC-A Composite / EAN-8 Composite / EAN-13 Composite
	    for(int i=startComposite; i>endComposite;i--) {
		if(SEPARATORCODE[i] == codeID) {
		    return ;
		}
	    }*/
	    return 0;
	}
	// urovo add shenpidong end 2019-11-28

	// urovo modify shenpidong begin 2019-11-28
	/*
	 * BarcodeCodeID byte[i] is code type
	 */
	private static final byte[] SEPARATORCODE = {
		0x49, // GS1-128     AIM ID is ]C1 , Honeywell ID is 0x49
		0x79, // GS1		 AIM ID is ]e0 , Honeywell ID is 0x79
		0x77, // DataMatrix  AIM ID is ]d2 , Honeywell ID is 0x77
		0x73, // QR code 	 AIM ID is ]Q3 , Honeywell ID is 0x73
		0x7D, // GS1 DataBar Expanded    AIM ID is ]e0 , Honeywell ID is 0x7D
		0x7B, // GS1 DataBar Limited    AIM ID is ]e0 , Honeywell ID is 0x7B
		0x64, // EAN-13 Composite    AIM ID is ]E0 , Honeywell ID is 0x64
		0x44, // EAN-8 Composite    AIM ID is ]E4 , Honeywell ID is 0x44
		0x63, // UPC-A Composite    AIM ID is ]E0 , Honeywell ID is 0x63
		0x45  // UPC-E Composite    AIM ID is ]E0 , Honeywell ID is 0x45
	};

	private static final byte[] AIM = {
		0x43, // first BarcodeAimID ASCII is C
		0x65, // first BarcodeAimID ASCII is e
		0x64, // first BarcodeAimID ASCII is d
		0x51, // first BarcodeAimID ASCII is Q
		0x65, // first BarcodeAimID ASCII is e (GS1 DataBar Expanded) , SEPARATORCODE is 0x7D
		0x65, // first BarcodeAimID ASCII is e (GS1 DataBar Limited) , SEPARATORCODE is 0x7B
		0x45, // first BarcodeAimID ASCII is E  (EAN-13 Composite), SEPARATORCODE is 0x64
		0x45, // first BarcodeAimID ASCII is E (EAN-8 Composite) , SEPARATORCODE is 0x44
		0x45, // first BarcodeAimID ASCII is E (UPC-A Composite) , SEPARATORCODE is 0x63
		0x45  // first BarcodeAimID ASCII is E (UPC-E Composite) , SEPARATORCODE is 0x45
	};

	private static final byte[] AIMMODIFIER = {
		0x31, // second BarcodeAimModifier ID ASCII is number 1
		0x30, // second BarcodeAimModifier ID ASCII is number 0
		0x32, // second BarcodeAimModifier ID ASCII is number 2
		0x33, // second BarcodeAimModifier ID ASCII is number 3
		0x30, // second BarcodeAimModifier ID ASCII is number 0 (GS1 DataBar Expanded) , SEPARATORCODE is 0x7D
		0x30, // second BarcodeAimModifier ID ASCII is number 0 (GS1 DataBar Limited) , SEPARATORCODE is 0x7B
		0x30, // second BarcodeAimModifier ID ASCII is number 0 (EAN-13 Composite) , SEPARATORCODE is 0x64
		0x34, // second BarcodeAimModifier ID ASCII is number 4 (EAN-8 Composite) , SEPARATORCODE is 0x44
		0x30, // second BarcodeAimModifier ID ASCII is number 0 (UPC-A Composite) , SEPARATORCODE is 0x63
		0x30  // second BarcodeAimModifier ID ASCII is number 0 (UPC-E Composite) , SEPARATORCODE is 0x45
	};
	// urovo modify shenpidong begin 2019-11-28

	// 4710 / 2100 / 4750
	// urovo add shenpidong begin 2019-11-28
	private static final int[] COMPOSITECODEIDS = {
		82, // CCA EAN-13
		83, // CCA EAN-8
		87, // CCA UPC-A
		88, // CCA UPC-E
		98, // CCB EAN-13
		99, // CCB EAN-8
		103,// CCB UPC-A
		104 // CCB UPC-E
	};
	// urovo add shenpidong end 2019-11-28

	// 6603 / 6703 , EAN-13/EAN-8/UPC-A/UPC-E/ return false , other return true
	public static final boolean isSupperCompositeCode(byte aim , byte aimModifier ,byte code) {
	    if(code == SEPARATORCODE[6] && aim == AIM[6] && aimModifier == AIMMODIFIER[6]) {
		return false; // EAN-13
	    } else if(code == SEPARATORCODE[7] && aim == AIM[7] && aimModifier == AIMMODIFIER[7]) {
		return false; // EAN-8
	    } else if(code == SEPARATORCODE[8] && aim == AIM[8] && aimModifier == AIMMODIFIER[8]) {
		return false; // UPC-A
	    } else if(code == SEPARATORCODE[9] && aim == AIM[9] && aimModifier == AIMMODIFIER[9]) {
		return false; // UPC-E
	    }
	    return true;
	}

	// 4710 / 2100 / 4750
	public static final int isSupperCompositeCode(byte[] data) {
	    if(data == null || data.length < 4) { // ]e0 length is 3 , data length must > 3
		return 0;
	    }
	    for(int i=0;i<data.length - 3;i++) {
		if(data[i] == 0x5D && data[i+1] == 0x65 && data[i+2] == 0x30) { // ]e0 is 0x5D 0x65 0x30
		    return i;
		}
	    }
	    return 0;
	}

	// Se4710/Se2100
	public static final int compositeIndexCode(String aimCodeIDStr , int codeID) {
	    if(SEAIMMODIFIERCODE[SEAIMMODIFIERCODE.length - 1].equals(aimCodeIDStr)) {
		if(codeID == COMPOSITECODEIDS[1] || codeID == COMPOSITECODEIDS[5]) {
		    return 8; // EAN-8 Composite max length is 8 , contains CCA EAN-8 and CCB EAN-8
		}
	    } else if(SEAIMMODIFIERCODE[SEAIMMODIFIERCODE.length - 2].equals(aimCodeIDStr)) {
		if(codeID == COMPOSITECODEIDS[0] || codeID == COMPOSITECODEIDS[4]) {
		    return 13; // EAN-13 Composite max length is 13 , contains CCA EAN-13 and CCB EAN-13
		} else if(codeID == COMPOSITECODEIDS[2] || codeID == COMPOSITECODEIDS[6]) {
		    return 12; // UPC-A Composite max length is 12 , contains CCA UPC-A and CCB UPC-A
		} else if(codeID == COMPOSITECODEIDS[3] || codeID == COMPOSITECODEIDS[7]) {
		    return 8; // UPC-E Composite max length is 8 , contains CCA UPC-E and CCB UPC-E
		}
	    }
	    return 0;
	}
	// urovo add shenpidong end 2019-11-28
	
	// urovo add shenpidong begin 2019-04-18
	// urovo modify shenpidong begin 2019-11-28
	// Se4710/Se2100 AIM Code ID Character
	private static final String[] SEAIMMODIFIERCODE = {
		"]C1", // GS1-128
		"]e0", // GS1/GS1 DataBar Expanded/GS1 DataBar Limited
		"]d2", // DataMatrix
		"]Q3", // QR code
		"]E0", // EAN-13 Composite / UPC-A Composite / UPC-E Composite
		"]E4", // EAN-8 Composite
	};
	// urovo modify shenpidong end 2019-11-28
	// urovo add shenpidong end 2019-04-18

	/*
	 * BarcodeCodeID contains one of SEPARATORCODE 
	 * (AIM[i]AIMMODIFIER[i]) ---> SEPARATORCODE[i] 
	 * AIM[0]AIMMODIFIER[0] ---> C1
	 * AIM[1]AIMMODIFIER[1] ---> e0
	 * AIM[2]AIMMODIFIER[2] ---> d2
	 * AIM[3]AIMMODIFIER[3] ---> Q3
	 * Data content of GS1 DataMatrix symbol
	 * ------------------------------------------------------------------------------------------------------------
	 * |FNC1|ES 1 (predefined length) |ES 2 (non-predefined length) | FNC1 or <GS> | ES 3 (non-predefined length) |
	 * ------------------------------------------------------------------------------------------------------------
	 * FNC1 value is ]C1、]e0、]d2、]Q3
	 * if contains C1/e0/d2/Q3(]C1、]e0、]d2、]Q3)  and separator decode.
	 */
	public static boolean isSeparatorDecode(byte aim , byte aimModifier , byte code) {
	    // urovo modify shenpidong begin 2019-11-28
	    if(code >= 0x44 && code <= 0x7D) {
		for(int i=0;i<SEPARATORCODE.length;i++) {
		    if(code == SEPARATORCODE[i] && aim == AIM[i] && aimModifier == AIMMODIFIER[i]) {
			return true;
		    }
		}
	    }
	    // urovo modify shenpidong end 2019-11-28
	    return false;
	}
	//N603================================================================
    public static boolean isSeparatorDecode(byte aim, byte aimModifier) {
        for(int i=0;i<AIM.length;i++) {
            if(aim == AIM[i] && aimModifier == AIMMODIFIER[i]) {
                return true;
            }
        }
        return false;
    }
    //// UPC-E Composite / UPC-A Composite / EAN-8 Composite / EAN-13 Composite
    public static final int compositeUCCIndexCode(int codeID) {
        if(Symbology.UPCE.toInt() == codeID) {
            return 8; // UPC-E Composite max length is 8
        } else if(Symbology.UPCA.toInt() == codeID) {
            return 12; // UPC-A Composite max length is 12
        } else if(Symbology.EAN8.toInt() == codeID) {
            return 8; // EAN-8 Composite max length is 8
        } else if(Symbology.EAN13.toInt() == codeID) {
            return 13; // EAN-13 Composite max length is 13
        }
        return 0;
	}
	//EAN-13/EAN-8/UPC-A/UPC-E/ return false , other return true
	public static final boolean ignoreUPCCompositeCode(byte aim, byte aimModifier, int code) {
	    if(code == Symbology.EAN13.toInt() && aim == AIM[6] && aimModifier == AIMMODIFIER[6]) {
		    return false; // EAN-13
	    } else if(code == Symbology.EAN8.toInt() && aim == AIM[7] && aimModifier == AIMMODIFIER[7]) {
		    return false; // EAN-8
	    } else if(code == Symbology.UPCA.toInt() && aim == AIM[8] && aimModifier == AIMMODIFIER[8]) {
		    return false; // UPC-A
	    } else if(code == Symbology.UPCE.toInt() && aim == AIM[9] && aimModifier == AIMMODIFIER[9]) {
		    return false; // UPC-E
	    }
	    return true;
	}
	// urovo add shenpidong begin 2019-04-18
	// urovo modify shenpidong begin 2019-11-28
	/*
	 * Se4710/Se2100 AIM/AIMModifier/code
	 * aimModifierCode value:]C1、]e0、]d2、]Q3
	 * if contains ]C1、]e0、]d2、]Q3 return true , other return false
	 */
	public static boolean isSeparatorDecode(String aimModifierCode , int codeID) {
	    boolean separatorDecode = false;
	    if(aimModifierCode == null || "".equals(aimModifierCode.trim())) {
		return separatorDecode;
	    }
	    for(int i=0;i<SEAIMMODIFIERCODE.length;i++) {
		if(SEAIMMODIFIERCODE[SEAIMMODIFIERCODE.length - 1].equals(aimModifierCode)) {
		    separatorDecode = compositeIndexCode(aimModifierCode , codeID) == 8; // EAN-8 Composite
		    break;
		} else if(SEAIMMODIFIERCODE[SEAIMMODIFIERCODE.length - 2].equals(aimModifierCode)) {
		    int compostCode = compositeIndexCode(aimModifierCode , codeID);
		    // EAN-13 Composite ~ 13 / UPC-A Composite ~ 12 / UPC-E Composite ~ 8
		    separatorDecode = (compostCode == 8) || (compostCode == 12) || (compostCode == 13);
		    break;
		}
		if(SEAIMMODIFIERCODE[i].equals(aimModifierCode)) {
		    separatorDecode = true;
		    break;
		}
	    }
	    return separatorDecode;
	}
	// urovo modify shenpidong end 2019-11-28
	// urovo add shenpidong end 2019-04-18

	public static void setSeparatorChar(byte[] sepChar) {
		// sepChar[] within the reasonable scope of the ASCII
		if(sepChar.length == 1) {
			setSeparatorChar[0] = sepChar[0];
		} else if(sepChar.length == 2) {
			// urovo add shenpidong begin 2019-04-18
			setSeparatorChar[0] = sepChar[0];
			// urovo add shenpidong end 2019-04-18
			setSeparatorChar[1] = sepChar[1];
		}
	}
	
	public static int isStartsWithSeparatorTable(byte[] source) {
		int sourceLength = source != null ? source.length:-1;
		if(sourceLength==STARTSWITH2) {
			for(int i=0;i<STARTSEPARATORTABLE.length;i++) {
			    if(source[0] == STARTSEPARATORTABLE[i][0] && source[1] == STARTSEPARATORTABLE[i][1]) {
				return i;
			    }
			}
		} else if(sourceLength==STARTSWITH3) {
		    for(int i=0;i<STARTSEPARATORTABLE3.length;i++) {
			if(source[0] == STARTSEPARATORTABLE3[i][0] && source[1] == STARTSEPARATORTABLE3[i][1] && source[2] == STARTSEPARATORTABLE3[i][2]) {
			    return i;
			}
		    }
		} else if(sourceLength==STARTSWITH4) {
		    for(int i=0;i<STARTSEPARATORTABLE4.length;i++) {
			if(source[0] == STARTSEPARATORTABLE4[i][0] && source[1] == STARTSEPARATORTABLE4[i][1] && source[2] == STARTSEPARATORTABLE4[i][2] && 
				source[3] == STARTSEPARATORTABLE4[i][3]) {
			    return i;
			}
		    }
		}
		return -1;
	}
	
	public static int getGSIndexOfSource(byte[] source) {
		for(int i=0;i<source.length;i++) {
			// ASCII 0x1D is GS , 0x1D = 0d29 (decimalism)
			if(source[i] == 0x1D) {
				return i;
			}
		}
		return -1;
	}

	private static byte[] getSeparatorDecode(int mode, boolean separator, int arrLastLength, int srcPos, int tarPos, int copyArrLastLength,
											 int arrNextLength, int arrPos, int arrTarPos, int copyArrNextLength, byte[] source) {
		byte[] arrLast = new byte[arrLastLength];
		if (separator) {
			arrLast[0] = setSeparatorChar[0]; // ASCII 0x28 is (
			arrLast[1] = source[0];
			arrLast[2] = source[1];
			if (mode == STARTSWITH2) {
				arrLast[3] = setSeparatorChar[1]; // ASCII 0x29 is )
			} else if (mode == STARTSWITH3) {
				arrLast[3] = source[2];
				arrLast[4] = setSeparatorChar[1]; // ASCII 0x29 is )
			} else if (mode == STARTSWITH4) {
				arrLast[3] = source[2];
				arrLast[4] = source[3];
				arrLast[5] = setSeparatorChar[1]; // ASCII 0x29 is )
			}
		}
		System.arraycopy(source, srcPos, arrLast, tarPos, copyArrLastLength);
		if (arrNextLength <= 0) {
			return arrLast;
		}
		byte[] arrNext = new byte[arrNextLength];
		System.arraycopy(source, arrPos, arrNext, arrTarPos, copyArrNextLength);
		byte[] result = separatorDecode(arrNext);
		if (result != null && result.length > 0) {
			byte[] decodeBarcode = new byte[arrLast.length + result.length];
			System.arraycopy(arrLast, 0, decodeBarcode, 0, arrLast.length);
			System.arraycopy(result, 0, decodeBarcode, arrLast.length, result.length);
			return decodeBarcode;
		} else {
			ScanLog.d("getSeparatorDecode , error!!! 0 result.len:" + (result != null ? result.length : -1));
		}
		return source;
	}

	private static byte[] separatorDecode2(int mode , int sourceLength , int indexOfSource , int[] startsWithSeparatorIndex , int indexStartSeparator , byte[] source) {
	    int maxLength = -1;
	    if(indexStartSeparator >= 0 && indexStartSeparator < startsWithSeparatorIndex.length) {
		maxLength = startsWithSeparatorIndex[indexStartSeparator];
		if(maxLength > sourceLength) {
		    maxLength = sourceLength;
		}
		    if(maxLength >= indexOfSource) {
			if(indexOfSource<=0) {
			    if(sourceLength > maxLength) {
				byte[] decode = getSeparatorDecode(mode , true , maxLength+2 , mode , mode + 2 , maxLength - mode , sourceLength-maxLength , maxLength , 0 , sourceLength-maxLength , source);
				return decode;
			    } else {
				byte[] decode = getSeparatorDecode(mode , true , source.length+2 , mode , mode + 2 , source.length - mode , -1 , -1 , -1 , -1 , source);
				return decode;
			    }
			}
			byte[] decode = getSeparatorDecode(mode , true , indexOfSource+2 , mode , mode + 2 , indexOfSource-mode , source.length-indexOfSource-1 , indexOfSource+1 , 0 , source.length-indexOfSource-1 , source);
			return decode;
		    } else {
			byte[] decode = null;
			if(indexOfSource > maxLength) {
			    decode = getSeparatorDecode(mode , true , maxLength+2 , mode , mode + 2 , maxLength-mode , source.length-maxLength , maxLength , 0 , source.length-maxLength , source);
			    return decode;
			}
			decode = getSeparatorDecode(mode , true , maxLength+2 , mode , mode + 2 , maxLength-mode , source.length-maxLength , maxLength , 0 , source.length-maxLength , source);
			return decode;
		    }
	    } else {
		if(indexOfSource<=0) {
		    return source;
		} else {
		    byte[] decode = getSeparatorDecode(mode , false , indexOfSource , 0 , 0 , indexOfSource , source.length - indexOfSource - 1 , indexOfSource + 1 , 0 , source.length - indexOfSource - 1 , source);
		    return decode;
		}
	    }
	}

	public static byte[] separatorDecode(byte[] source) {
		int sourceLength = source != null ? source.length:-1;
//		ScanLog.d("separatorDecode , sourceLength:" + sourceLength + ",STARTSEPARATORTABLE4:" + STARTSEPARATORTABLE4.length + ",STARTSEPARATORTABLELENGTH4:" + STARTSEPARATORTABLELENGTH4.length);
		if(sourceLength > 0) {
			if(sourceLength > STARTSWITH2) {
			    byte[] startsWithSeparator = null;
			    boolean maybeSeparator4Startswith = false;
			    boolean maybeSeparator3Startswith = false;
			    int indexStartSeparator = -1;
			    int indexOfSource = -1;
			    int maxLength = -1;
			    if(sourceLength > STARTSWITH4) {
				/*
				 * 31(0-6)  31n(n=0/1/2/3/4/5/6) , if(n=0) ASCII is (0x33 0x31 0x30)
				 * 32(0-9)
				 * 33(0-7)
				 * 34(0-9)
				 * 35(0-7)
				 * 36(0-9)
				 * 39(0-3)
				 */
				if((source[0] == 0x33) && (source[1]>0x30 && source[1]<0x37 || source[1] == 0x39)) {
				    if((source[1] == 0x39) && (source[2]>=0x30 && source[2]<0x34) || (source[1] == 0x31)&&(source[2]>=0x30 && source[2]<0x37) || 
					(source[1] == 0x32 || source[1] == 0x34 || source[1] == 0x36)&&(source[2]>=0x30 && source[2]<=0x39) || 
					(source[1] == 0x33 || source[1] == 0x35)&&(source[2]>=0x30 && source[2]<0x38)) {
					maybeSeparator4Startswith = true;
				    }
				}
				/* 
				 * 700n(n=1、2、3)
				 * 703s , s ASCII value is 0x73
				 */
				if(!maybeSeparator4Startswith && (source[0] == 0x37) && (source[1] == 0x30) && (source[2]==0x30&&source[3]>0x30 && source[3]<0x34 || 
					source[2]==0x33&&source[3]==0x73)) {
				    maybeSeparator4Startswith = true;
				}
				/*
				 * 800n(n=1~8)
				 * 8018
				 * 8020
				 */
				if(!maybeSeparator4Startswith && source[0] == 0x38 && source[1] == 0x30 && ((source[2]==0x30 && source[3]>0x30 && source[3]<0x39) || 
					(source[2]==0x31&&source[3]==0x38) || (source[2]==0x32&&source[3]==0x30))) {
				    maybeSeparator4Startswith = true;
				}
				/*
				 * 810n(n=0、1、2)
				 * 8110
				 */
				if(!maybeSeparator4Startswith && source[0] == 0x38 && source[1] == 0x31 && (source[2]==0x30 && source[3]>=0x30&&source[3]<0x33 || 
					source[2]==0x31&&source[3]==0x30)) {
				    maybeSeparator4Startswith = true;
				}
				if(maybeSeparator4Startswith) {
				    startsWithSeparator = new byte[] {source[0] , source[1] , source[2] , source[3]};
				    indexStartSeparator = isStartsWithSeparatorTable(startsWithSeparator);
				    indexOfSource = getGSIndexOfSource(source);
				    if(indexStartSeparator>=0) {
					maybeSeparator4Startswith = true;
				    } else {
					maybeSeparator4Startswith = false;
				    }
				}
				
			    }
//			    ScanLog.d("separatorDecode , indexStartSeparator:" + indexStartSeparator + ",indexOfSource:" + indexOfSource + ",maybeSeparator4Startswith:" + maybeSeparator4Startswith);
			    if(!maybeSeparator4Startswith && sourceLength > STARTSWITH3) {
				// 24x or 25x
				if(source[0] == 0x32 && (source[1] == 0x34 || source[1] == 0x35)) {
				    maybeSeparator3Startswith = true;
				} else if(source[0] == 0x34 && (source[1] == 0x30 || source[1] == 0x31 || source[1] == 0x32)) { // 40x or 41x or 42x
				    maybeSeparator3Startswith = true;
				} else {
				    maybeSeparator3Startswith = false;
				}
				if(maybeSeparator3Startswith) {
				    startsWithSeparator = new byte[] {source[0] , source[1] , source[2]};
				    indexStartSeparator = isStartsWithSeparatorTable(startsWithSeparator);
				    indexOfSource = getGSIndexOfSource(source);
				    if(indexStartSeparator>=0) {
					maybeSeparator3Startswith = true;
				    } else {
					maybeSeparator3Startswith = false;
				    }
				}
			    }
//			    ScanLog.d("separatorDecode , indexStartSeparator:" + indexStartSeparator + ",indexOfSource:" + indexOfSource + ",maybeSeparator4Startswith:" + maybeSeparator4Startswith + ",maybeSeparator3Startswith:" + maybeSeparator3Startswith);
			    if(!maybeSeparator4Startswith && !maybeSeparator3Startswith) {
				startsWithSeparator = new byte[] {source[0] , source[1]};
				indexStartSeparator = isStartsWithSeparatorTable(startsWithSeparator);
				indexOfSource = getGSIndexOfSource(source);
			    }
//			    ScanLog.d("separatorDecode , indexStartSeparator:" + indexStartSeparator + ",indexOfSource:" + indexOfSource);
			    if(maybeSeparator4Startswith) {
				// startsWithSeparator length is 4
				byte[] decode = separatorDecode2(STARTSWITH4 , sourceLength , indexOfSource , STARTSEPARATORTABLELENGTH4 , indexStartSeparator , source);
				return decode;
			    } else if(maybeSeparator3Startswith) {
				// startsWithSeparator length is 3
				byte[] decode = separatorDecode2(STARTSWITH3 , sourceLength , indexOfSource , STARTSEPARATORTABLELENGTH3 , indexStartSeparator , source);
				return decode;
			    } else {
				// startsWithSeparator length is 2
				byte[] decode = separatorDecode2(STARTSWITH2 , sourceLength , indexOfSource , STARTSEPARATORTABLELENGTH , indexStartSeparator , source);
				return decode;
			    }
			} else {
				return source;
			}
		} else {
			ScanLog.i("separatorDecode , source:" + source);
		}
		return null;
	}
}
