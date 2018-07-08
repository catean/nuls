/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.accout.ledger.rpc.dto;

import io.nuls.account.ledger.base.util.AccountLegerUtils;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.utils.AddressTool;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "inputJSON")
public class InputDto {

    @ApiModelProperty(name = "fromHash", value = "来源output的txHash")
    private String fromHash;

    @ApiModelProperty(name = "fromIndex", value = "来源output的outIndex")
    private Integer fromIndex;

    @ApiModelProperty(name = "address", value = "转入地址")
    private String address;

    @ApiModelProperty(name = "value", value = "转入金额")
    private Long value;

    @ApiModelProperty(name = "lockTime", value = "锁定时间")
    private Long lockTime = 0L;

    public InputDto() {
        this.lockTime = 0L;
    }

    public InputDto(Coin input) {
        this.fromHash = AccountLegerUtils.getTxHash(input.getOwner());
        this.fromIndex = AccountLegerUtils.getIndex(input.getOwner());
        this.address = AddressTool.getStringAddressByBytes(input.getFrom().getOwner());
        this.value = input.getFrom().getNa().getValue();
        this.lockTime = input.getFrom().getLockTime();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getFromHash() {
        return fromHash;
    }

    public void setFromHash(String fromHash) {
        this.fromHash = fromHash;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }

    public Long getLockTime() {
        return lockTime;
    }

    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
    }
}
