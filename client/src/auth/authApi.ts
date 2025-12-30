export async function loginWithEmailPassword(mailId: string, password: string) {
  const organisationId: string = import.meta.env.VITE_ORG_ID;
  const res = await fetch("http://localhost:8080/api/v1/auth/token/login", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify({ mailId, password, organisationId }),
  });
  if (!res.ok) {
    throw new Error(await res.text());
  }
  return res.json();
}
