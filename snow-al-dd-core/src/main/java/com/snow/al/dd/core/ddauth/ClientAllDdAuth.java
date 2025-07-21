package com.snow.al.dd.core.ddauth;

import com.snow.al.dd.core.ddauth.vendor.authidentifer.DdAuthIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class ClientAllDdAuth {
    private final String clientId;
    private Set<DdBankAccountAuth> bankAccountAuthSet = new HashSet<>();
    private Set<ClientAuth> clientAuthSet = new HashSet<>();

    public void addClientAuth(ClientAuth clientAuth) {
        clientAuthSet.add(clientAuth);
    }

    public void addBankAccountAuth(DdBankAccountAuth bankAccountAuth) {
        bankAccountAuthSet.add(bankAccountAuth);
    }

    @RequiredArgsConstructor
    @Getter
    public static class ClientAuth {
        private final String vendorGroup;
        private String clientIdCode;
        private String clientName;
        private String clientPhone;
        private String clientAuthCode;
        private Instant validFrom;
        private Instant validTo;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ClientAuth that = (ClientAuth) o;
            return Objects.equals(vendorGroup, that.vendorGroup) && Objects.equals(clientIdCode, that.clientIdCode) && Objects.equals(clientName, that.clientName) && Objects.equals(clientPhone, that.clientPhone) && Objects.equals(clientAuthCode, that.clientAuthCode) && Objects.equals(validFrom, that.validFrom) && Objects.equals(validTo, that.validTo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vendorGroup, clientIdCode, clientName, clientPhone, clientAuthCode, validFrom, validTo);
        }
    }

    @RequiredArgsConstructor
    @Getter
    @ToString(exclude = {"bankAccountAuthIdentifier"})
    public static class DdBankAccountAuth {
        private final String vendorGroup;
        private final DdAuthIdentifier<DdBankAccountAuth> bankAccountAuthIdentifier;
        private String bankAcctNum;
        private String bankAcctName;
        private String bankAcctIdent;
        private String bankAcctPhone;
        private String authCode;
        private Instant validFrom;
        private Instant validTo;
    }



    @RequiredArgsConstructor
    @Getter
    public static class DdBankAccount {

        private String bankAcctNum;
        private String bankAcctName;
        private String bankAcctIdent;
        private String bankAcctPhone;





    }
}
