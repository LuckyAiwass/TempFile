/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.server.sign;

import android.content.Context;
import android.os.Build;
import android.util.Slog;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class UrovoSecurityPermission {

	private Context mContext;
	public static final String TAG = "SecurityPermission";
	public static final boolean UROVO_PRIVATE_PROJECT = (Build.PROJECT.equals("SQ26TB") || Build.PROJECT.equals("SQ27T") || Build.PROJECT.equals("SQ27TC") || Build.PROJECT.equals("SQ27TE") || Build.PROJECT.equals("SQ27TD"));
	public static final String Pubkey_Platform_Private = "e8a13a9270d2aea501fc3014ca093e51d3365579acf856e91d7c1e3077d6169d56a7091b8660db22f86d83aff475a3dbd811aa51db036027684baca345a0d8c13e92f2d97dcf8bf8041cbca60b859b8b51cc8ebd9ec26a3a653dc6d56d999c0ef4338fee022701f14db5030873c8c0fd93c17da7c28e3b65f004853c1a09ba9539101a8baee0ebd3ef9da97aaa6ed545495f540f1d6b1bcd0ecf12306fe326b7abf0eb76d1ab0d54f949f58fa3ee0d87d64788c38b004da55f2046ba404b91b89e78cdeda54f7c4a652f3f51fc1f207d821b3666db5bc48f5cb66c2a053466c2aeb5ef3c07d9bd228a96c28a29d796e06432b93464b3d6127660427c9fbccf11";
	public static final String Pubkey_Platform_Debug = "c869241fb144a2501007f942ec7e367319775f419decb3a3443092ce3bf1d873e42becb7f26a54178379965a890153cc0fc1ca5bbf1a7400764f8205a92eb398857ebfd37d103170905a0a3dc7eca80e77e26696cdcbc94afe3c71cb35b5e8cb3cdc2d933bc3b85df4f096b4b25042b8aab4d8241f5e9179754888318505cb4b4d61444b3c086abe86ed5349f9995d7173b2cf0c0499eff36de9b17ee74e3ea539f157b0904f18a14504e71537042ed3e0a85666f62286bddca6ab6d04dbf441f76aa8709c6cba197f40aade61c9773bd427959b4df13e96fa736a5081a4b112b42221dd18eb4e9e17f3057f688e26eb33175ed15661814b102e3df080a7f751";
	public static final String Pubkey_Platform = UROVO_PRIVATE_PROJECT ? Pubkey_Platform_Private : Pubkey_Platform_Debug;
	
	public static final String Pubkey_Testkey_Private = "d8674aca90c8e27cde8be1e10798d5d7a5e4fc051b893ddfade26895bde4107bb9be594e68ba8d2cf7622243052d024b6ee5c31ca5c79cce0224aed2ec64692bf30624727ba6ecfab095356cad1dd965baeeec9a6e3e61fde148de38dd4d4c195e78811643b5efc857b47fc8b7c108ecc76569e1b9d276baa1214057a93350fb86ce96509b88ac4689105be4d8a46cdffa689ffa3cdb5cd9b9caa75b99f21e111d43606c337e26f5278010b77d51acc427be543f4f656229397b66b1bef02e7184f72b7dab7419526862a9d1a70efb2b3618a04e690d4b6c41b6ba1a8fc208ceb0d93ca5096cc8d64d43c19b9586e8f2816ba382d33e925a106383cd85c7b201";
	public static final String Pubkey_Testkey_Debug = "c2bb962188f54c2a50bdac9becb2db93f4847e256c806c6e847382151fd3f838bc12afc004c6df0f266c1414095be9999e87ea2d58ae79e6ebbf284ea24f4df1be5862c7758703927f4262a00d3ebfd8f39b194cb030b1ed6d1f3df0bdbc67d0dfa2979b1844dfd517692c9fb81235cce29d538ea3fa23bd05ef63d6504dabab2c8448852c1f11f67d662c9dc8fec31be7e8a5c96a3f408de34b84c8a347975874d1ef20f64da81b13fc17fcaf2484f28e33deeec4237e8623f03b61549819ab905e7c3de02005169019c85b150bd16aa62a61a650afd9502d4ee2162d06dacb76ce0250a163d82413e6f1b93d4fc08ca1e837ee1aa89ddd78bc989f887dea6b";
	public static final String Pubkey_Testkey = UROVO_PRIVATE_PROJECT ? Pubkey_Testkey_Private : Pubkey_Testkey_Debug;
	
	public static final String Pubkey_Media_Private = "f51d243c2ef7880ff03cf73dcbacdd6e66c3b038c7a4b65ab8b1a732b1c15ae1efadb32cf5955d96c65bfbaf93c2dec9c9c566963404f4c96ecdfe85c8f729ea5e0fccbf0fa666d28db4ded2ef2143b1d968cd63cdf7ea22510466936a98491c43498092c9cf106ea830bc5530823a234585c51d369816a704b31cc7fe59741913e9352fa563992a5e686dacc0f05f5011bd047b97e9fa73d23d61862c4d671c1d83ad646e61ca659d4ba99f4913a6e302ca07004d5df7a57bfcba3e925d766194acb0a5a8a185af3adc587e1f8d04852e1be71941de0fc52e30bbb0d3ffc96e1cabde6becabc954c37038ccc7102e47495b3e55e76a4d3baad2389c6c3c291f";
	public static final String Pubkey_Media_Debug = "a1ea0fe8718ff0278eea2a891dbe104c1e43026b5b20dad069ca3a90728a8e5434c28297fd39bb89db5225399358130f00b1f98314a804a1ca14f2f6c3df076cd9e6d2e494323a874146b70935b32bbbb89818a3850d05c837a55c5701d9b4ff3f0485ea21ed14ca16ff30fd01591e81db4a363a11539083312ec87514919c65e8108db6e9d6ba1b84425d4c33c3aca75e52dc11ad45078d0cbe5b44dc47bb577820074469deaa1fd5eab168afb1fd8fa3ba7781bb7113a32cb2997eb26a0a8d7a9302347fd959d6d710ee9ef0d4f39e9cab6daa7bd2d9f83270d35a1b5ab5a39e2d4e1afaf9dd49d6c0b42429a040022d93e888aecbbbe41265ec916d7f23fd";
	public static final String Pubkey_Media = UROVO_PRIVATE_PROJECT ? Pubkey_Media_Private : Pubkey_Media_Debug;
	
	public static final String Pubkey_Shared_Private = "eb997fee1e8dc6def186c04b3f320cc39179d7cf357669af1fe481d7af46eb7241698657fd53b231a22b3c7375d8aa045f0e7cfb1e154efe1eec394d198b9a447f960ab35a1e0e557a32bdb44b2eedf15abde2dd0f2340b5454d24cf6ce7bb0c4a2f5a9bcf9be253d8f8834c73437349ac408a6406eb0fe8b62d0a650a0a5d779a1cb1b51cd12663632ee99e19b938e155b4d7ad39e869c78dd67e40c70a975fa79ea3d637b692243801da7cac756b82a7871c023f1bb869a915891615affdfbdad8b0ff23c86a63b0217f1be743646fa1fdaff791f9f8fa33e0f35c8c10b2f9942cea6a0ea0478699e873b7ac521e495156d79092159b58b8f9dd110f0021c5";
	public static final String Pubkey_Shared_Debug = "bb8452ab4d28c5f9597ac077f25ee1edc415caea012a35e7ccd82467d35fcc4b8d30b4ad9cc4072d66a871c0d9ac5faf7f2ba3e33393daa90fbb498851372f760845cfc8fcb4b9e5c6f3078595f4d33f3feea2f01deb111b54dc1137c96a5d26b89a5a39c069267c481eba9f931dfe27cee53c0081dea3e67b7dfaafaaa0d2db66d73b14e5f1ec093e88c17b307d48edc6408f5a5b473b07b65efd29d14c257c8f86dbec2e9f37ab0948633da178d5d443ea7ee3cdcbfd899331e936026839c82135b6a6eb8604f4ace2dbf65b645fa86bca506ac7fe680fdf85815c9c5ad8d72014c106afdcc04579a94d805838c1f6aecdfc93ad5b72639ea545fe5b5c658d";
	public static final String Pubkey_Shared = UROVO_PRIVATE_PROJECT ? Pubkey_Shared_Private : Pubkey_Shared_Debug;
	//SQ26 release
	public static final String Pubkey_Release = "df75edf50cb1dc16c0050ff86f0f2b638d2c6277cb93a78068194e50ce6121e1237be8b8926b51e39642a5b6263095d6578a8c0ad85a31fdc52c119211f08df9de410af4fa50905a0a16fcc53ae11bc5fd091213ea9a25a1dd3b022d551b69b38ae00cd9b7b4f83c02df95a7e88c1994a9c502bfe3809d35af521313461a12f2f2b98d973792e7f4b13e63b1be7c9fe471205e5abb374b6070364fe79ada1a62b460e8d89a97711d5291362e5aea13bf86efaa5520593e1b675cf80de97bf7d6c5dea1fc70d9f281d8be365986163778b29e98bbf98ff49b979e05e13337086449fc9a455ed04acaec039f3bf695965b8a860156a02bb657502d73900ca7694b";
	//SQ26 app key
	public static final String Pubkey_User = UROVO_PRIVATE_PROJECT ?
"81fad4cce408bb2659b8a9f58d5c2e51ab58608fa7b5ff2a029a2b30853d35affb297ea316922cbce784e71ebc8cdf23f19cb575fcacbbcf8453c750ddb8d16bc33652123bb8c3c886ffaf8e6189a913b96cb37ae4d2833e0227a3415c67bbb565769dac078990f3b4570b8f1906aeadb3b7a2cddbf743368d238216212626a8c1306f7278cea1876185fcdd1eca2bc5ac7fd32dea06f6341f4540b087731b93c689bc16acfc3cdd3af1d11642b7f009b8a837fd6d3e322974ae7c62869bc5042489ea294d6a7c0f515a73c5c48249fd199ede94040e45bfeda3267361b88062e32cda6739d65fed88977db9e281336c93fa7c3d453592d31c8f304049188c6f"
: "96a96797fb65091c8c288818f1e0f911a3f539e66dc003695302b513249b5489af2168b5a43c77b8bee887d32e8fbe049efd45eaf886e28a8ba3eb2e5651ef0c528a7325c4df2f1e0bed828fd8b5de92ade76d97486085ddbf4d9dffff039479519323f1b390f25d39ddfce5db379df96dabb6b405951cf9db7159835355c6afb8803bac6731325091beaf8516569c5100a13e450c6b32a94eaef2ae5d384c1afb9661d29bb6c672240b72d4bf4ae86db8d9353040e42223bfd95c3c8fa89b90a9b895c6b6fc1263f43c8a541f33f78234f05d3b9016aacbc28ea3d86c294883b4c04f325417c72b0486de8af2294bb80e0d5b782ee4cd79d084ca2bb325b00f";

	public static final String Pubkey_HSY_User = "c680e7337815d87636bfbc4327dc8f31381a375bd3c6ea75aa005d747ff2cd9633b16f815371e4016facdd84b1b6bdbca5d6fd435342e83c84d76d0ef9b910883985123867298ab9d4f99f4a9f51bdda222b8f5e99a4cf644d0f9171e419d547f5d1bff760048566e8359b35bab6be6b873246f6cee913f18b1cb27581f3bdaa3c924096e9f9a94d7536cfd6d2502f174d71e0bc0582e1b5a7895f0d21a11dae54e422e09606e8fe7a2ef8aa5868d55eb262702664912faef022c1d189defae394bf61dedec23e03919537df3f8940ece504675e3fdc4396a85a74339cfb0748e309cf077011254b0427ee8b35eef5560a706ef660d848f2e25e7b91a753a6ff";
	public static final String Pubkey_BHW_User = "863fb8013f65dc752ad87950e02817d923b3a8b7169564e42da7087cc0a5de46de08ce67181f1aa5358225b63ad6f082008bb50a726797223dab30f221b072ca5e58fb609e163d8b9f5ec09d39174fc8fca419d3fd786484d8f9c33f078ddc1ec132d3384937707776b31c2f4f8e9cbe2c99c54452a93d0708ec7fa46152696b06f54879c982b2c480efdcbccf2bab346d859e41119aeb546d218303530ca4e2b1039b7eab969bb565be0ef7d1a770f284490e7c2801eb1b9b7fee380d9042ddbdaae972e7279f044aeec397f6d5009d43022e9dd5fbf5b810a605e8e82ac409efaacb84816d1c2cf85921a6fe565b1d59fb6f88040b6a59f17f997bc861db3d";
	
	private static final String BHW_PRIVATE_User = "a24bc6b331764927147afcfdf74b2e7bf2a37158d21241d9b9b0c13c399ce2342d7546cecc7e72782b35695a7300b5781191b5ec107cb24658aacb7e34e1d159372dab6ebf6a1703d910f8d80c862eb299d3b7732cac50dc110e3721897c14408335060069dd9dab3f6df5aa2c2d819a34c2121b0140e5bf013e751c6f34127f5ff24b7b4f43afef116d36f73447eba8a3c75d0ab6848f9c63dc74d789ec474bb84d6d732829ce48cc18ac8092f1785ab73b6d077f82c73f0fcdd286c68a4fbf0583e8c8544c8da90b47a3b3c13f2b75aaf454cd45e2b3d49128976e36d07eb14e64f68ffefd569f36f4ccbe925e043afee576842ffc35c6bbbfdf4e7af9f5bd";
	public static final String PkEs = "8ab11bf6c1c13b2fce8f112d5d2298c4ea0344e6acf1b5ff14c2f1bb5afca8d7d350668cfd374a37923291afebcd10ece43ec05f0673ada1b3fb680e93b6c3ba6746d11b3666caea4ccc6cb80d03188fedce9decbdc8585a918034f26854a8e4f4ed98a67ae5a929f5d374bf2b4a683a34e188040188075af3207002d48b5f87";
	public static final String PkSogou = "81d94ccb646b5ceb38c8912a932a9d991c2b006ec0f0270ed837937588727fd0c24dc6465343501edb1ce85ab213dd434376d0bec1a6c808608c47f60d8dbe454a95d477c68d2a0da7cd84fa68590c4416f3319c246d3d5a05002565e6ab03de6ee2bec083af1e85b7bb2c601a884d1ff648801f951e14bb2d515ce446bc399b";
	public static final String PkMxPlayer = "a749e6529ae11eac5c159e6ea3d73b96d5ca3d0f0044f8ea3081b28374ef3c389297020274c57607afe7391c1ec5ce557c68ff7b820f24a2f0675d59e5f8eccbad11bdc61d0e45095d34869223f8dffd04879457df1791b37821677024bfcfe1f4db58781121a45d65f4ab73edb3b6a1d0836cf1a3f4721dd5c15645f337658d92d730e58741e3f2182e12ec450f7ac6189f4170d25c4e667d7105f7f49d6de0ae6229c11198525a57c2a93c54e4f646ad734ed0fc0d430f1713a9a7d1c0b51ca07d5de425da7a1d7c6fcd678aebd663597a5f8f89c542e36a4aecc5d12e95b28939294f3d469fa2f4c6f6b508ff7e6ed0ede8e4ddab4b80ec0a7770d648a781";
	public static final String PkBaiduInput = "9c7a58a39572c4b379ddfca6765e95d3aec69fe362ce622e629647cf441b9e4b7b695e540fd29b7da7b2ab64793089f2b69112d11ac5776973dd68cff88b671826c1286e57c7294c76c7c118ae41bf9336ff9ae0aa90c65ed7db0749ff137b815b6d3b53abaad72d7817b0b8900caef12eea13d12baf0b8cb30543bfb3489c23" ;
	public static final String PkWifiLock = "d6931904dec60b24b1edc762e0d9d8253e3ecd6ceb1de2ff068ca8e8bca8cd6bd3786ea70aa76ce60ebb0f993559ffd93e77a943e7e83d4b64b8e4fea2d3e656f1e267a81bbfb230b578c20443be4c7218b846f5211586f038a14e89c2be387f8ebecf8fcac3da1ee330c9ea93d0a7c3dc4af350220d50080732e0809717ee6a053359e6a694ec2cb3f284a0a466c87a94d83b31093a67372e2f6412c06e6d42f15818dffe0381cc0cd444da6cddc3b82458194801b32564134fbfde98c9287748dbf5676a540d8154c8bbca07b9e247553311c46b9af76fdeeccc8e69e7c8a2d08e782620943f99727d3c04fe72991d99df9bae38a0b2177fa31d5b6afee91f";
	public static final String PkgEs = "com.estrongs.android.pop";
	public static final String PkgSogou = "com.sohu.inputmethod.sogou";
	public static final String PkgMxPlayer = "com.mxtech.videoplayer.pro";
	public static final String PkgBaidu = "com.baidu.input";
	public static final String PkgWifiLock = "opotech.advancedwifilock";
	private static final String PkWeiXin = "c05f34b231b083fb1323670bfbe7bdab40c0c0a6efc87ef2072a1ff0d60cc67c8edb0d0847f210bea6cbfaa241be70c86daf56be08b723c859e52428a064555d80db448cdcacc1aea2501eba06f8bad12a4fa49d85cacd7abeb68945a5cb5e061629b52e3254c373550ee4e40cb7c8ae6f7a8151ccd8df582d446f39ae0c5e93";
	private static final String PkgUCBrowser = "aac959f5439f1595907c7fa43a6d628fa6c6e0006470d122ee5edac296e51d24450acf16e3a4aa8b75735e23a8a7cd4925825a9e3311d6c6d4024b4e837d613bb037a25e898380625b042c1cb7eb017f86772b4ae10256f840d75a9b4f646f2fd7a178e58035182358c1eb2b940307107af050384f3b2763b186679e371ea5c9";
	private static final String PkgQQInput = "8a9faf7f1aef003633a5fd7eae1e501dad5e2ed8be917dd1ad9dfde8053815d0c7de32fcd115d010c52dc3f8623b886db5876051b5d1c721062c3f6c94b7060af2da0d77fe7c3d0f94fd3f15dbf8db4110ea04adcf09bc4dc47e8e1113cb7dccfe83e795067cc639506a5866966affc8092cd3624f360aca07ffdf25a303d535";
	private static final String PkgQQ = "com.tencent.qqpinyin";
	public static final String PkgBaiduMap = "com.baidu.BaiduMap";
    public static final String PkgBaiduMapKey = "9c7a58a39572c4b379ddfca6765e95d3aec69fe362ce622e629647cf441b9e4b7b695e540fd29b7da7b2ab64793089f2b69112d11ac5776973dd68cff88b671826c1286e57c7294c76c7c118ae41bf9336ff9ae0aa90c65ed7db0749ff137b815b6d3b53abaad72d7817b0b8900caef12eea13d12baf0b8cb30543bfb3489c23";
    public static final String Pkgifly = "com.iflytek.tts";
    public static final String PkgiflyKey = "877aaa1fc89c0d1f0b8ac5578c18f7e29bf823f5684861ffaf0fbf90f92a7d6bb247582969104c1a788a26e7345399325bb6a5a81ccce6a5009fc84172ed4eac22e1dce4735377c345dfaa3b92aa00c0192daa5c8d5b1c09a078596b35dc8549982063cc9364bd051cadc3adf39d81dae025c5c1021d66ad510bc6cc16ebe7af";
    private static final String PkgWandaAppSotreKey  = "b8518b04de6d0f3ea0f31eeb4b61e5ce44bd52347ba31b5147648ab4e475cc10667e2cbe773f87d169420e4ff74514cf57de2131a61b3b4277171818cb6131d454edc1897f3a23562fd3b3a12aeac2f8c9b5ef7ac86ba5061406eecfeed8591bf15262c29981bd0374d0d1319704e84e6ebfd83a5d4b12950a87352eb32f913f4aa6114359e41d010680832f311edc7b6e8b6d04ddf52ac65901c30cbeb82079c32d63549337d5786c93619098f061de0fcdcfa248d6885967b998f1a5eae8cd2e8f43768db055f2c85d2dd0e276c4d710fc413e192f62d1dbacb61ebc6d9aad56c718dd7d0076e281a9bb9bf47828b6b3614f9347686c267c4dfc652ad0c6b9";
    private static final String PkgWandaAppPublicKey = "a70cc4f6dd2359dfa730df336d00553f9dff5b4e6592ccca7832cffa93434b1eb21bfad1122dfdd1177f94b0f2a7577bf64940fc3c1b1d8ba26c756ec5921e85a5706ccd31bab22a627b1a4a17ba2e131966cd2e5cd7e199dfdcf4e92b5ddb1c5dbfa24e600d605eb99cd6770d85f699e809595bccf7e5182bb9ccbcaabc7771da0d09f89b38f1d5098f5e3ed20e87beb28e3a45e536c71f40edddf3f15d086bc8edfc8ec1ee6e99e33499bcd9f2a168fb986c4026c83137459635060557bd89cfb2d986693f1b0227eb0cdc0ecd1187546efbd8730f334cac70ce182a9dbb35fa21af8891342617e39d09e5d8ac73f47df4db0a9a03d5a00c94ea0afbb06b09";
    private static final String PkgXunFeiInput = "9f25eeca258b92be6c7163828df85668e0ffd273dbe884ee0c7dbad2b8a93cd4e46b22f64b2d0ba55b237aabd62cce08aae096215ff6ba36f6a745eacd4cd1f29816432ae539266ab54a4a9fccc8ab6de4ee34f6bbd38447c1902298e3159d228cddee42e86f4932bc6a7d5db000f8051658ff12bffd39e4d1350c5662029db1";
    private static final String PkgXunFei = "com.iflytek.inputmethod";
    private static final String Pkg99billKey = "a1c812b55295d21596fa1047986c2a664919753b15abef90b40b6a064e7a4272ee556fe9b0797668647deceb914a2ec0bfc7c956e7fa9278e98e87fa35f8442f8bee2820a38c78a56e08ab4adbdbe75c667f9f83ec83dd325fa8fab7a31de260e6a06aa85e4039f21643e9f1508c350a290aadb71a7bb787ac5bd7ad8f0d1357";
    private static final String PkgLYNXKey ="d276e34130ffcc3a3aebc969a2564e44b659bbef504be845f8cd076caf6cddc96bdf96f492ce6e856eee489c54a6b9e61baf47ef642a664cf799852567b083f236a6df376cd6bf44ee5251592ddcad97778f52e882d30fbcc65138f6613655481ad4947f829e357a5ea77ddef9d84f764e83d51b0d284ef93328a921c155d1fdb4dd3d734d921ec39329955ee81b9619740ef49bef4004cdea7cd55f5d3502eb429809d13643ea690717bb768cbdce6066bee90ed0a81bf7e2ad34d69125c90bad490104ad2a3f0f86a75c5334170746de0d90ba360e1c176f86d3b271845bc7dca6a38a3d4c75b4e792073cd7cdbb1cde239e98ac8d0fff5e8a01fe531669e3";
    //Voice Map GoogleContactsSyncAdapter NetworkLocation
    private static boolean ENBuild = android.os.SystemProperties.getBoolean("pwv.custom.enbuild", false);
    private static final String PkgGooglePlay = "com.android.vending";
    private static final String PkgGoogleKey ="ab562e00d83ba208ae0a966f124e29da11f2ab56d08f58e2cca91303e9b754d372f640a71b1dcb130967624e4656a7776a92193db2e5bfb724a91e77188b0e6a47a43b33d9609b77183145ccdf7b2e586674c9e1565b1f4c6a5955bff251a63dabf9c55c27222252e875e4f8154a645f897168c0b1bfc612eabf785769bb34aa7984dc7e2ea2764cae8307d8c17154d7ee5f64a51a44a602c249054157dc02cd5f5c0e55fbef8519fbe327f0b1511692c5a06f19d18385f5c4dbc2d6b93f68cc2979c70e18ab93866b3bd5db8999552a0e3b4c99df58fb918bedc182ba35e003c1b4b10dd244a8ee24fffd333872ab5221985edab0fc0d0b145b6aa192858e79";
    //Talk.apk
    private static final String PkgGoogleTalkKey ="efe7f26efd5e0947ea58c2b93f7993e036552b83379616ea46c46f2dadcb0653c77918039d9ce7fc03a39a1b5274f49e0917d70f803220f60beeda5ac5c4c161cc60da1600a3462249b4cc908d9cae5c9f27c807e3d95dae0b477fe096f516308f740338506c2e02a5831ab71a207dd026d7e4bfe1d3ec28d8a9d6da004866c1";
    private static final String PkgKoolPosKey = "817fbed872de1560290e4f5124d2f15f8c644190df0199dd542f3537facbf8afde24276251245c79685e7460c9d0dc6677fa20bf91e82a2ec208396b8422d3757ae52c05616ca667059f68b73c671d3019cd2d751e674678516b9cfc058da28070238f4698528523e2c554b60b7ae53eb540639587405eee0d2d58cfb81572dce11b8bfeab6a57f1281ee39770be62468cd7a39dca543512f547dd4566cd88c180be1328067dd7a77e91173987f8ae2ccdf4da9489fbbba76f378769ea0ada3bc4a7835862dab99e7635b2c75cbce6d00a506446a3297a59bfd1caea400d93a7f8e3cbdf0cb1ba08ef6526ef592cfb47160af0f9d96dab72196b636392edd755";
    private static final String PkgGoogleInputKey = "9f48031990f9b14726384e0453d18f8c0bbf8dc77b2504a4b1207c4c6c44babc00adc6610fa6b6ab2da80e33f2eef16b26a3f6b85b9afaca909ffbbeb3f4c94f7e8122a798e0eba75ced3dd229fa7365f41516415aa9c1617dd583ce19bae8a0bbd885fc17a9b4bd2640805121aadb9377deb40013381418882ec52282fc580d";
    private static final String PkgGoogleInput ="com.google.android.inputmethod.pinyin";
    
	private static final String PKGGOOGLEKEY1 = "c30f88add9b492096a2c586a5a9a80356bfa026958f8ff0c5dfaf59f49268ad870dee821a53e1f5b170fc96245a3c982a7cb4527053be35e34f396d24b2291ec0c528d6e26927465e06875ea621f7ff98c40e3345b204907cc9354743acdaace65565f48ba74cd4121cdc876df3522badb095c20d934c56a3e5c393ee5f0e02f8fe0621f918d1f35a82489252c6fa6b63392a7686b3e48612d06a9cf6f49bff11d5d96289c9dfe14ac5762439697dd29eafdb9810de3263513a905ac8e8eaf20907e46750a5ab7bf9a77262f47b03f5a3c6e6d7b51343f69c7f725f70bcc1b4ad592250b705a86e6e83ee2ae37fe5701bcbdb26feefdfff60f6a5bdfb5b64793";
	private static final String PKGGOOGLEKEY2 = "d6931904dec60b24b1edc762e0d9d8253e3ecd6ceb1de2ff068ca8e8bca8cd6bd3786ea70aa76ce60ebb0f993559ffd93e77a943e7e83d4b64b8e4fea2d3e656f1e267a81bbfb230b578c20443be4c7218b846f5211586f038a14e89c2be387f8ebecf8fcac3da1ee330c9ea93d0a7c3dc4af350220d50080732e0809717ee6a053359e6a694ec2cb3f284a0a466c87a94d83b31093a67372e2f6412c06e6d42f15818dffe0381cc0cd444da6cddc3b82458194801b32564134fbfde98c9287748dbf5676a540d8154c8bbca07b9e247553311c46b9af76fdeeccc8e69e7c8a2d08e782620943f99727d3c04fe72991d99df9bae38a0b2177fa31d5b6afee91f";
	private static final String PKGGOOGLEKEY3 = "a7bfad6ced1fac874d865965ae947b050643e5b2695c3e890efb1521e6b305173ee478c93e86e9518504fa7fbd36b28183a43d8958e1dfa48d123cc20303c09b67327b7a24d657b3accf04e9d0b12c730311a7c3c5be290915c627cbcebae6ba51b9747809d271e6d363052ee6631a3affc19b6899e1e1795148fd955025eccdc7d7c3229688d7780bd7392c6e8c96968bc89ffca2dbf345d954d23891faee835a4563aabe104a604faca8aa81f6cda7e2daa70ec5249724b653a5ed25aed098447c899e8b4a5ebba52823fd0b8ef7ce17b46e13d00bee202ee5d023865deaa208a0078ab43d1ddeba669eae103930de4e126fe8922779f25ccd8af5bfea1a7f";
	private static final String PKGGOOGLEKEY4 = "c6ed6855f4af969924c01b3d530080ad59676f5d4e7f7556c1b15efda1aab428ec8a6a132d8998fbcf9e832c4a2b9301ef94710cbca0a0d0d9a6154807750c6232fa7491c854eddcb48801dc12064075a93e1af8eaa4a6c47a6d2357417435f25090bbd811118de1dd0958050032cd1c70e2144696b5c0cee4b3d447385beeb1e272c6e431bad4c00c47c0a517d5d2a9b98607793267bd00d17f4e3d863188c58fd44c5a62fa5a56a8f265972b3f0afebed5bca02b86054b47a41507dcea06fbdb3660c7202c6eef08af5ac0a0ea94f7094a9200023eae1ace203919a4ab165132c90b49359b84fe29fb7029f236334d507a635fc33159984d644c70f5b499a7";
    //MoR jwbp.crt leo.crt kykg.crt  kyzc.crt    test.crt
    private static final String PkgMoRKey1 = "8a001525cc963ca93c601312aed4e524c1a9124a447ef28acd11147b20de9936aa1971d64078c3ee13dea2706525e7ab6e5ad0ee37c26c8dfad004a8330e17cf2f5546f4e9ebb95f1ec9dff8d8b8e6023ca8f76065399e9431be2a27827d15912c5cdcb359e6615b9795dda04ac7090bf2f5714022c373389e05a61d78d792c67844e4c980814cf2d76613a57afab426e1325ffd49be87e0a7088914448f55f27eea8a5e900d47f9d9caa2980be85a11a0794259f8b2e895c55ccc97661fbc891ac85629b509feb65e0321b4884002ec8c6f9defa3d134e5e451f76eda8831a8c0a84308d306e9b89f63115d59cf26218426ef3673a83a0dbb4044cc474fea33";
    private static final String PkgMoRKey2 = "a08eceffa0c5721b730906fa9008d2d82de38573bbd9fe2f0591588ecf3c9f024a5b3b4724abd5d329ff54b630983929ce8875768fae9def1c3250445f9938cc52ae0d82d9a389a1fbd4a5aff0c7109843bd3b08761ca9701535f17424f724b068c527502337db0b2e6df1cd598ee1259cc874c07c6eba3ae873f51a3a3736ec4551b0c5749b2b904fe18da5ab3b4644960a009ecdc0ad6e1ebc967ff3dd95a65820b0eb0aaee591f1533ea094e770ebf63fe164b458dfbff350dbecd50bdd9a8d0a219c38a97dd0366c47b867c69d5692829af38cc4c572f619f2cd172df8e572720a6e4c495dd97131d9039cb8aefeb1a19497b616f1e532ddb9586794938b";
    private static final String PkgMoRKey3 = "a126256865b6dd8604cad487cdd1e07d813d07d329c7ce513ef211eea9ce56428486c634018816d06cb87040277cf6226f13906ea69a391f6f7ca06ddc740201d15851c50b6b84edc8738770c350816c96bcbd1bbfe5973f0e21acb928c2f0de10c997739d7b6121bb5bbccae6cea91257866ae54e6b02395d112b67b1379d73f724f2931d7dfc4b734e3a4f7f3e3de90702b73d1d829722d77713c467865306706bb94b84ed958196c93974ae1c3f58adb1da3d2b2b9e7b68f6d5e1f7e2d689abfacc24432f9cede1477b1d7fd6bd17f262f887469aa00f1ca9e501392763f6b06e8b059a4c854125e938bbd2003c960671b94a39303e2ab61691346f09d863";
    private static final String PkgMoRKey4 = "99f3795a7c91fd8332212dc1ac4d4962b6d84ddb4b7ab6e5fc80063b1c031c63af09e381603dca78d60be1dda3e2a9a4b2667bf6e27acdfb4a1b096a6934c08e9878951c702df21f4fc1c9bf30e6fb4d81274b575bd1b88d740c8325fbbc65d51bab3f31881510782be39954cdc2b5d4391585d9c3cb97bf72e23e909f923c3b416b9c765c4ece36a0915b38c1b5e7a8476837ecb56f99a08e3d7b515737725ca00d0fe0404bc71b7cdd9f4d817ce1b4496a2b8402969546886d98d214591448b04c749bf695202c7220dfb4fc0a64efdafa4742bbec2160271a2e6c254d2c0aa528bd4e0813b36bc4b31ffbb79a9dd17ed64a81a57db4b120a2b9be34947051";
    private static final String PkgMoRKey5 = "8df49dc15bc88d2cf732271cb672fa6ba504010f8db1c371ba94072f94a28103df9c5aedbb3ecd09ccb467e28b75b9390e1778843b92a91bc9df11bd17175271fa8c2e728f38e843b13136cfac3bbfe96f817e616af69ae521d6125bfb28fa8c84b70723c4ca1e7fa8c44d4673c278bc8ca5e49fc8274c35c6942b86a77f0068092caf715802127ccc161cb51ddc80273b9d9fdae32e1367b3f376993bad6b8fb4104d934d0ffb26f9a01e043ef7e852dcb2999389ec601258814253d802ddcf2bdc24212e5174a6d7fd7d9211e29abcf5624346bb312c114c987bae899145660f882eaf3fc80bebb2b612679c998131283fc068f34f558040d127ad798b0fc9";
    private static final String PKGMT_MKeyinput = "816f755da975222a4639206580da58e68bf5700af2eb14830d45c8acf639e18be1a4d5168afa36d6c68eeb1c6b4f53fb769b601253c9f500bdca55581101ce79865ea9121b669fb7ec4be280bc8fae9278e465b3c4f75bd1cacd011d27f5cafd740618cc33fe396684289bc4e5364d2a837d6f516d9d5f05715485007976a477";
    private static final String ZTE_WT_MPublicKey = "8db6c7aa04b6c5cdfd7f0428e508cbd63812cbfe6a11b3a5cc5cd58b71f3c6f881b21c7f5f9b1f932bac1b75138d9c9570480d079eea8963a1a3c46201019acd2e1b2b10b01070e321d179e73f8dc93080dad1310edf80dd6d143f5a675764c433e0dcee0092b427000d247de866d869e7eb2db26f0d3fa5b67ffff229eb77822249323a52264ccd317beb03a9f828392c2a1c032ce2811432e6628bb45cf5c3ff5046ff2f7af75bffc7e26e6e2b8cd2e11aabd9ada8190ea00f462a7ce872861440a178095d47d5198df4429470c2adb9e3c8f8c11b7e35a4af875b28e412ff2c2ece7ed9087578ce1042b68ea71b9a1fc609382330687fbf176f650f09487d";
    private static final String HEADINGPublicKey = "a9cb09171c685ef99bfcf9c1d5886f09c787a2fd960bbae50e7f4081e50125a8fd5f29ce600d23df19b346770f5b679c59fbca9313c824243cf2a996b09d234f6446b7dcb8f5dc2a9276a14ff9b73525c0f73ddf253c2f22209f553caa1d322ae48b8f99ecda7a58a5242c03e8d2d6a229a726df1ae00294a0104c63c303aa55";
    private static final String PkgPostMan =
"aa07bfddf8bdd2cc3f0f17430c40c4eaefb9e3f7f65fce0f73518561fbf59ca54f06d542fbb3593c8ed4660748136d28d09d0a6872ef744b0a6f67ca5fb2123575c94fa23569de2b682a632f027c8e7f427772b7ab2b4dc014d7fd108cd9ec01e1b9ccb2b1c05cb4c62a2cb221f1164ec4ef35de5fc21f3a9480d3f466cbbae7";
    //  TODO
    private String[] customCerts = null;
	public UrovoSecurityPermission() {

	    /*try{
	        UfsManager manager = new UfsManager();
	        if(manager.init() == 0) {
	            int certCount = manager.getCertCount();
	            if(certCount > 0) {
	                customCerts = new String[certCount];
	                byte[] cert = new byte[1024];
	                for(int i = 0; i < certCount; i++) {
	                    int ret = manager.getCert(i, cert);
	                    if(ret > 0 &&  ret < cert.length) {
	                        customCerts[i] = (new String(cert, 0 , ret));
	                    }
	                }
	            }
	        }
	        manager.release();
	    } catch(Exception e) {
	        e.printStackTrace();
	        Slog.d(TAG, "UfsManager read cert error!");
	    }*/
	}

	public UrovoSecurityPermission(Context context){
		this.mContext = context;
	}

	public String parseSignature(byte[] signature) {
		String signInfoPubKey = null;
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(signature));
			signInfoPubKey = cert.getPublicKey().toString();
			Slog.w("TAG", "signInfoPubKey_a=" + signInfoPubKey);
            int start = signInfoPubKey.indexOf("modulus=");//pubKey:OpenSSLRSAPublicKey
            if(start == -1) {
                start = signInfoPubKey.indexOf("=");//pubKey:OpenSSLDSAPublicKey
                start += 1;
            } else {
                start += 8;
            }
            int end = signInfoPubKey.indexOf(",");
            if(end == -1){
            	end = signInfoPubKey.length();
            }
            signInfoPubKey = signInfoPubKey.substring(start, end);

			/*signInfoPubKey = signInfoPubKey.substring(signInfoPubKey.indexOf("modulus=") + 8,
					signInfoPubKey.indexOf(","));*/
//			Slog.w("TAG", "signInfoPubKey_b=" + signInfoPubKey);
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		return signInfoPubKey;
	}

	/**
	 * com.google.android.androidforwork PkgGoogleKey
	 * com.google.android.configupdater PkgGoogleKey
	 * com.google.android.backuptransport PkgGoogleKey
	 * com.google.android.feedback PkgGoogleKey com.google.android.gsf.login
	 * PkgGoogleKey com.google.android.onetimeinitializer PkgGoogleKey
	 * com.google.android.partnersetup PkgGoogleKey com.google.android.gsf
	 * PkgGoogleKey com.google.android.syncadapters.calendar PkgGoogleKey
	 * com.google.android.syncadapters.contacts PkgGoogleKey
	 * com.google.android.tts PkgGoogleKey com.google.android.gms PkgGoogleKey
	 * com.android.vending PkgGoogleKey com.google.android.webview PKGGOOGLEKEY4
	 * com.google.android.gm PkgGoogleKey com.google.android.apps.maps
	 * PkgGoogleKey com.google.android.googlequicksearchbox PkgGoogleKey
	 * com.google.android.youtube PkgGoogleInputKey
	 * 
	 * 
	 **/

	public boolean cmpSign(String signInfoPubKey, String packageName) {
		if ((signInfoPubKey.equals(Pubkey_User) && (!Build.PWV_CUSTOM_CUSTOM.equals("BESTVA") && !Build.PWV_CUSTOM_CUSTOM.equals("SZT")))
				|| signInfoPubKey.equals(Pubkey_Platform)
				|| signInfoPubKey.equals(Pubkey_Testkey)
				|| signInfoPubKey.equals(Pubkey_Media)
				|| signInfoPubKey.equals(Pubkey_Shared)
				|| signInfoPubKey.equals(Pubkey_Release)
				) {
			Slog.d(TAG, "Device Security- " + "allow " + packageName
					+ " to Install");
			return true;
		} else if ( !UROVO_PRIVATE_PROJECT && ((signInfoPubKey.equals(PkBaiduInput) && packageName
                        .equals(PkgBaidu))
                || (signInfoPubKey.equals(PkEs) && packageName
                        .equals(PkgEs))
                || (signInfoPubKey.equals(PkgiflyKey) && packageName
                        .equals(Pkgifly))
                 || (signInfoPubKey.equals(PkgBaiduMapKey) && packageName
                        .equals(PkgBaiduMap))
                || (signInfoPubKey.equals(PkSogou) && packageName
                        .equals(PkgSogou))
                || (signInfoPubKey.equals(PkMxPlayer) && packageName
                        .equals(PkgMxPlayer))
                || (signInfoPubKey.equals(PkWifiLock) && packageName
                        .equals(PkgWifiLock)))) {
		    Slog.d(TAG, "Device Security- " + "allow " + packageName
                    + " to Install");
		    return true;
		} else if( ((signInfoPubKey.equals(PkBaiduInput) && packageName.equals(PkgBaidu))
                || (signInfoPubKey.equals(PkSogou) && packageName.equals(PkgSogou))
                || (signInfoPubKey.equals(PkgXunFeiInput) && packageName.equals(PkgXunFei))
                || (signInfoPubKey.equals(PkgQQInput) && packageName.equals(PkgQQ))
                || (signInfoPubKey.equals(PkgGoogleInputKey) && packageName.equals(PkgGoogleInput)))) {
            Slog.d(TAG, "Device Security- " + "allow " + packageName
                    + " to Install");
            return true;
        } else if(ENBuild && ( signInfoPubKey.equals(PKGGOOGLEKEY4) ||signInfoPubKey.equals(PkgGoogleTalkKey) || (signInfoPubKey.equals(PkgGoogleKey) && !packageName.equals(PkgGooglePlay)))) {
            Slog.d(TAG, "Device Security- " + "allow google apk"
                    + " to Install");
            return true;
        } else if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("WD") && (signInfoPubKey.equals(PkgWandaAppPublicKey) || signInfoPubKey.equals(PkgWandaAppSotreKey))) {
            Slog.d(TAG, "Device Security- " + "allow " + packageName
                    + " to Install");
            return true;
        }  else if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("WPH") && (signInfoPubKey.equals(PkgPostMan))){
            Slog.d(TAG, "Device Security- " + "allow " + packageName
                    + " to Install");
            return true;
        }  else if(customCerts != null) {
            int len = customCerts.length;
            Slog.e(TAG, "customCerts Security- len" + len);
            for(int i =0; i < len; i++) {
                if(signInfoPubKey.equals(customCerts[i])) {
                    return true;
                }
            }
			/*Slog.e(TAG, "customCerts Security- " + "do not allow " + packageName
					+ " to Install");*/
			return false;
		} else {
            Slog.e(TAG, "Device Security- " + "do not allow " + packageName
                    + " to Install");
            return false;
        }
	}

	public boolean cmpUserSign(String signInfoPubKey, String packageName) {
        return false;
    }
}
