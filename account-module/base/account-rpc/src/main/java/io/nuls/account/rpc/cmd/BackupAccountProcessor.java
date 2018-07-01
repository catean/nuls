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

package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.kernel.model.Address;
import io.nuls.account.rpc.model.AccountKeyStoreDto;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 */
public class BackupAccountProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "backup";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> the account you want to back up - Required")
                .newLine("\t[path] The folder of the export file, defaults to the current directory");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "backup <address> [path] --backup the account key store";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 2 || length > 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!AddressTool.validAddress(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String path = args.length == 3 ? args[2] : System.getProperty("user.dir");
        RpcClientResult res = CommandHelper.getPassword(address, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        RpcClientResult result = restFul.post("/account/export/" + address, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        AccountKeyStoreDto accountKeyStoreDto = new AccountKeyStoreDto((Map<String, Object>) result.getData());
        Result rs = backUpFile(path, accountKeyStoreDto);
        if (rs.isFailed()) {
            return CommandResult.getFailed(rs.getMsg());
        }
        return CommandResult.getSuccess((String)rs.getData());
    }

    /**
     * 导出文件
     * Export file
     *
     * @param path
     * @param accountKeyStoreDto
     * @return
     */
    private Result backUpFile(String path, AccountKeyStoreDto accountKeyStoreDto) {
        File backupFile = new File(path);
        //if not directory , create directory
        if (!backupFile.isDirectory()) {
            if (!backupFile.mkdirs()) {
                return Result.getFailed(KernelErrorCode.FILE_OPERATION_FAILD);
            }
            if (!backupFile.exists() && !backupFile.mkdir()) {
                return Result.getFailed(KernelErrorCode.FILE_OPERATION_FAILD);
            }
        }
        String fileName = accountKeyStoreDto.getAddress().concat(AccountConstant.ACCOUNTKEYSTORE_FILE_SUFFIX);
        backupFile = new File(backupFile, fileName);
        try {
            if (!backupFile.exists() && !backupFile.createNewFile()) {
                return Result.getFailed(KernelErrorCode.FILE_OPERATION_FAILD);
            }
        } catch (IOException e) {
            return Result.getFailed(KernelErrorCode.IO_ERROR);
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(backupFile);
            fileOutputStream.write(JSONUtils.obj2json(accountKeyStoreDto).getBytes());
        } catch (Exception e) {
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        return Result.getSuccess().setData("The path to the backup file is " +  path + File.separator + fileName);
    }

}
