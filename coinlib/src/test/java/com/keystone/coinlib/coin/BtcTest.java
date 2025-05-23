/*
 * Copyright (c) 2021 X1-BTC-PSBT-Firmware
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file LICENSE.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.x1-btc-psbt-firmware.coinlib.coin;

import com.google.common.collect.Lists;
import com.x1-btc-psbt-firmware.coinlib.ExtendPubkeyFormat;
import com.x1-btc-psbt-firmware.coinlib.coins.BTC.Btc;
import com.x1-btc-psbt-firmware.coinlib.coins.BTC.Deriver;
import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class BtcTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "3Kd5rjiLtvpHv5nhYQNTTeRLgrz4om32PJ",
                "352BwoAuZWavjSDgAUUCnbg6a2v3FEzbMn",
                "32Hhx9CM3gYwChY2ZRFtHTYpJCeUaoypkj",
                "3C5VmDeH3x6x9fPm4cft3qVhzvv8R4Ln7K",
                "38c8FFS9W4QEW55WXgo2wX8HZJCutH89VT",
                "3EWeeC2wPyTAsPG7rTRbczbFjvZ8nW41AZ",
                "35yk3RdCHQYptsZAmibMpUt6F9XkK7UVCm",
                "32sNjcbmj4hqthuP6hsQk3BjzVLFLZSLr8",
                "34dKWKb347TJwp1PysTt5faqSs9C1PXrVX",
                "3AzuY1VfwKBgAN3soFnm8BNNAJxx4S3Y7L",
                "3CAgK3r8nDiMcdnqBUL3Yy7JMFDjvkXqHn"
                };
        String pubKey = "xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver(true).derive(pubKey,0,i, Btc.AddressType.P2SH_P2WPKH);
            assertEquals(address,addr[i]);
        }

        addr = new String[]{
                "1NKH6iXtjQjzjndJsKJogj5AbwTJZYeqDX",
                "15NxdDkZkiVtze5GVmYwTw2R1BpKekyz1g",
                "1PGcHJ7Uz2dm8qDjbCR4UVvMyyEpbUX1Bf",
                "1GwwKRB7f5eUfkQXpznJHfvcfE77rNo9dY",
                "1A3S3Vp9gMBiXAUb287FCuCvGpCUvBxf1",
                "15z9mWewNTBMGeX1oQAQRj4ko9j56wzSnq",
                "1KmyhQKERuDwaXgGD4wxvSAavxGhxjFdCa",
                "1KtoDzC8RJQEUGFGo6awDXfLKX2DzdwDmd",
                "1BrrVPCciPnvSpYMWjAunMhFYfsN1a2SsZ",
                "15MprQnv9rBgWwdMP5Gav5atYSvMFBmSf4"
        };
        pubKey = "xpub6CHASsYUXq4Z1vSdcayG7v7mjUoABEcRBEKWjoEGBKmjKsEZ765J9ivEpYqcznQbnCmMFnJSkKp5f58sAgDUyfCfab5qq9eQjBBjpsb6tju";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver(true).derive(pubKey,0,i, Btc.AddressType.P2PKH);
            assertEquals(address,addr[i]);
        }
        addr = new String[] {
                "bc1q9unqc738dxjg5mk8zqtz33zg59cahrj29s24lp",
                "bc1qxgjq769zjm4npchn3d5p0pl0w32qexycxt3efv",
                "bc1qd8jkgnrt4qgnneutqrz6s8tzncdjtztsvhhnmc",
                "bc1qe2wrgssc8fkvjhwy3mcypcqcqnpf02zkmtthvj",
                "bc1q8tkjkn0fdf775xy670dvwcxg3pvdkg93qqpule",
                "bc1qech02hjkr0s4aajae3ulnxq30c2z6cqkfj0umf",
                "bc1qtksmexnnpdlf6gylzkhlnsyk76aa38fxqggk6u",
                "bc1qmr785dwsgjk7genr3u2dy3rxvjwug9s5zdk5q5",
                "bc1q568s0pc4859remhuqtgpf90ssrug29t4uzp796",
                "bc1quexfwqe4kxgchzhupecws7ld4xqfdu8es9jpat"
        };
        pubKey = "zpub6rcabYFcdr41zyUNRWRyHYs2Sm86E5XV8RjjRzTFYsiCngteeZnkwaF2xuhjmM6kpHjuNpFW42BMhzPmFwXt48e1FhddMB7xidZzN4SF24K";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver(true).derive(pubKey,0,i, Btc.AddressType.P2WPKH);
            assertEquals(address,addr[i]);
        }
    }


    @Test
    public void deriveTestnetAddress() {
        String[] addr = new String[] {
                "2N4J1WajwKZKpRtzzUmaW9B5GEqkppqdVY9",
                "2N5vzpJv9yHyi8aCREm1DfMfdZcEPeLKFKh",
                "2N3cagc4up2Xoz7BufANWrppQXethv76LUv",
                "2N3aTw1qLWkqtHcdCJ5eWhsuQQckbuyuzkN",
                "2Mz1sUMfKi6hiBNYMgn71Q5NpSeexmr76MF",
                "2MwmYudTmtY7yPiMcpyoFL5oBZiUq6M5tTM",
                "2MyxPa8GmEcynJojy4bUeb5G3y9nHGUrj5i",
                "2Mx3FigCdk7WfWSJMBkH3wMHALYZJoXH4VH",
                "2MzvSdkBeoFSLpVQSgnpp2daZiyEf2iMqin",
                "2MstUHV6svqiiwzCyykkuTSKdCYUiKSfAbC",
                "2N9iKrXVw2rk9iZWUhReCZtx8tzDNgTWyfP",
        };
        String pubKey = "upub5EALNCP4Y7P3JQtLi1Rzms8CBztexbPxZRNkxa9YcnhFx4w9BMbH7EMehatPCv8KruffErPyVcdxA7aceFPhdDyHKof7UrZ6uD9rVExiqsq";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver(false).derive(ExtendPubkeyFormat.convertExtendPubkey(pubKey,ExtendPubkeyFormat.xpub),0,i, Btc.AddressType.P2SH_P2WPKH);
            assertEquals(address,addr[i]);
        }


        addr = new String[] {
                "tb1qk5my3s95fqzuy9e8adaj8e2yrnj3sempjqeskv",
                "tb1qwqhnxk06d9kv8nyseky3s7drtfn289ez93cenx",
                "tb1qj2zy686vq69pts87g9ghd36fh22pdts0q4pzcf",
                "tb1qtahjsjxm0pfcarhar43tt0u5567leum28umfry",
                "tb1qyarwllvn2ez0kzeqhl67xtxhel9l8wx62jq0vz",
                "tb1qx2tgev0n7vac6uxkmf83d4v52nkxtsyuu5v3h0",
                "tb1qltnyye32afq57ewnrvgu2qqjy7yvruawzgs0x9",
                "tb1qg4pase9pv5pj0afgf6y2y382cslxzahquqev3y",
                "tb1qg2ukt2y7zrx6lfzc3snpjadlsleg00qfq2mwxt",
                "tb1qynv707xunc89jqpmc2nygaltl0dmres2yhu3qh",
        };
        pubKey = "vpub5Z3SXQwuvQWt5vBQiRYrqhbou6BB7u1TFcA4DTQxPirU4oqMwnWW5DcSmM31h7SzofmUM3xHHn8rEht38jyuX8tfXS2D1desPVRsvnD5Dtr";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver(false).derive(ExtendPubkeyFormat.convertExtendPubkey(pubKey,ExtendPubkeyFormat.xpub),0,i, Btc.AddressType.P2WPKH);
            assertEquals(address,addr[i]);
        }
    }


    @Test
    public void testCreateMultiSigAddress() {
        String pubkey1 = "0375e00eb72e29da82b89367947f29ef34afb75e8654f6ea368e0acdfd92976b7c";
        String pubkey2 = "03a1b26313f430c4b15bb1fdce663207659d8cac749a0e53d70eff01874496feff";
        String pubkey3 = "03c96d495bfdd5ba4145e3e046fee45e84a8a48ad05bd8dbb395c011a32cf9f880";

        List<byte[]> pubkeys = Lists.newArrayList(Hex.decode(pubkey1), Hex.decode(pubkey2), Hex.decode(pubkey3));

        String address = new Deriver(true).createMultiSigAddress(2, pubkeys, MultiSig.P2SH_P2WSH);
        assertEquals("3PA7HYj6x6xmk9WPGfrwqeKckYcQyNPdS7", address);

        address = new Deriver(false).createMultiSigAddress(2, pubkeys, MultiSig.P2SH_P2WSH);
        assertEquals("2NEiKMHf8ZZU7ww8vwoUpTbJsxtpai1VjtM", address);

        address = new Deriver(true).createMultiSigAddress(2, pubkeys, MultiSig.P2WSH);
        assertEquals("bc1qwqdg6squsna38e46795at95yu9atm8azzmyvckulcc7kytlcckxswvvzej", address);

        address = new Deriver(false).createMultiSigAddress(2, pubkeys, MultiSig.P2WSH);
        assertEquals("tb1qwqdg6squsna38e46795at95yu9atm8azzmyvckulcc7kytlcckxsey6dra", address);
    }

    @Test
    public void testCreateMultiSigAddressFromXpub() {
        String xpub1 = "Zpub75H51BQ73jcN2d8VcnScwnwKwrfRsA7UXL1LHmoxmXmGmFgCwxs4raek66GYG5sBdqnyS7whyR4c36Ky4x7Rfo7sVkfS7hvJNEUtX6LexH2";
        String xpub2 = "Zpub75fLJ4Y3UMxgoL2N6XLecqLj2Gt6JJ6U5t48PwB6bTEnWUgkhG3vavhnh8ZZwj3mA9acN7gU1NaqNXzvZyTKJrZLK7q7JqcamdoFhpgtizf";
        String xpub3 = "Zpub755NbzNDFus6egJM22CLBqjabHyNV8PEWJHGBkfCSXN2W8HRPKEA2MyxvycgD4AxNMcrTuxSwKYiNJ19h9PLURGZoWyC2Tutr7SWaU1swYK";

        String[] expect = {
                "bc1qf4fqa472u8xlf9ghdrp4v2xxcxfh4d6ee7f8478ywecfpthh6vus8n7fws",
                "bc1q6uk9x0202nvrt27ne28njljyt25cuta26f6g2wju7y8at88x43cqg9n98j",
                "bc1qn3mk6h9n4wvspsv5w4f78ez97mzf4kq5a73cs8zsajdz5ugvudes550k2a",
                "bc1q7ul39ax900kg65psk6g5es8hjfxkfnfcq7mp4zz9f9nwm93hjx9sfzu40a",
                "bc1qmfhfrd7dqqd3x66d6aw04e96xxec74gnkhkgtpwtngmwm9n2hdnq3m3n72",
                "bc1qekcvc8n8wpld7zgxrtycyf7cn7s9vyz0fx70zjm7quakl7d309dqc9ez96",
                "bc1q27wugty8uy3zp3ujfvdmf4fn0u76g5ufrm699sfxrrdlv5xkkvask0fvmj",
                "bc1qf26d8y6u2c3qkt66cwdkrwh9gvun9dq6rxftmmrm8umgjugnmh4q65rlwj",
                "bc1q86h2qmukyqyflt3z7gw4q6975kakcvkv24utq63d76h4pzccpp5sjw4gd2",
                "bc1q3cg55awjrqdc7gwyxvdccuhhrttkxn9lpau45u4zelf0sq349wfqunwryw"
        };
        List<String> xpubs = Lists.newArrayList(xpub1,xpub2,xpub3);
        for (int i = 0; i < expect.length; i++) {
            String s = new Deriver(true).deriveMultiSigAddress(2, xpubs, new int[]{0, i}, MultiSig.P2WSH);
            assertEquals(expect[i],s);
        }

        expect = new String[] {
                "tb1qf4fqa472u8xlf9ghdrp4v2xxcxfh4d6ee7f8478ywecfpthh6vussmgx5l",
                "tb1q6uk9x0202nvrt27ne28njljyt25cuta26f6g2wju7y8at88x43cqld92aa",
                "tb1qn3mk6h9n4wvspsv5w4f78ez97mzf4kq5a73cs8zsajdz5ugvudesrueesj",
                "tb1q7ul39ax900kg65psk6g5es8hjfxkfnfcq7mp4zz9f9nwm93hjx9s72264j",
                "tb1qmfhfrd7dqqd3x66d6aw04e96xxec74gnkhkgtpwtngmwm9n2hdnqxn8uy9",
                "tb1qekcvc8n8wpld7zgxrtycyf7cn7s9vyz0fx70zjm7quakl7d309dq0d0dl4",
                "tb1q27wugty8uy3zp3ujfvdmf4fn0u76g5ufrm699sfxrrdlv5xkkvasp8lrpa",
                "tb1qf26d8y6u2c3qkt66cwdkrwh9gvun9dq6rxftmmrm8umgjugnmh4qdu4s5a",
                "tb1q86h2qmukyqyflt3z7gw4q6975kakcvkv24utq63d76h4pzccpp5s9xr8h9",
                "tb1q3cg55awjrqdc7gwyxvdccuhhrttkxn9lpau45u4zelf0sq349wfqtmcv7p"
        };

        for (int i = 0; i < expect.length; i++) {
            String s = new Deriver(false).deriveMultiSigAddress(2, xpubs, new int[]{0, i}, MultiSig.P2WSH);
            assertEquals(expect[i],s);
        }

    }

    private static String reverseHex(String hex) {
        byte[] data = org.spongycastle.util.encoders.Hex.decode(hex);
        for(int i = 0; i < data.length / 2; i++) {
            byte temp = data[i];
            data[i] = data[data.length - i - 1];
            data[data.length - i - 1] = temp;
        }
        return org.spongycastle.util.encoders.Hex.toHexString(data);
    }
}
